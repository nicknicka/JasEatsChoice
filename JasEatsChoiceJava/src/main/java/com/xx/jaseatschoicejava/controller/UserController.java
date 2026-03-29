package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.LoginRequest;
import com.xx.jaseatschoicejava.dto.UserDTO;
import com.xx.jaseatschoicejava.entity.RegisterRequest;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import com.xx.jaseatschoicejava.service.AliyunSMSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private com.xx.jaseatschoicejava.service.WalletService walletService;

    @Autowired
    private com.xx.jaseatschoicejava.service.UserPreferenceCacheService userPreferenceCacheService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseResult<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 验证验证码
            if (registerRequest.getCaptcha() == null || registerRequest.getCheckCodeKey() == null) {
                return ResponseResult.fail("400", "验证码不能为空");
            }

            // 使用验证码工具类验证验证码
            boolean isValidCaptcha = captchaUtil.validateCaptchaAndDelete(registerRequest.getCaptcha(), registerRequest.getCheckCodeKey());
            if (!isValidCaptcha) {
                return ResponseResult.fail("400", "验证码错误或已过期");
            }

            // 检查手机号是否已被注册
            if (userService.isPhoneExists(registerRequest.getPhone())) {
                return ResponseResult.fail("400", "该手机号已被注册，请更换其他手机号");
            }

            // 检查邮箱是否已被注册
            if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty()) {
                if (userService.isEmailExists(registerRequest.getEmail())) {
                    return ResponseResult.fail("400", "该邮箱已被注册，请更换其他邮箱");
                }
            }

            // 创建User对象并设置属性
            User user = new User();
            user.setPhone(registerRequest.getPhone());
            user.setPassword(registerRequest.getPassword());
            user.setNickname(registerRequest.getNickname());
            user.setEmail(registerRequest.getEmail());

            // 调用注册服务
            boolean success = userService.register(user);
            if (success) {
                return ResponseResult.success("注册成功");
            }
            return ResponseResult.fail("500", "注册失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "注册失败");
        }
    }

    /**
     * 用户登录
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private com.xx.jaseatschoicejava.util.CaptchaUtil captchaUtil;

    @Autowired
    private AliyunSMSService aliyunSMSService;

    @Autowired
    private com.xx.jaseatschoicejava.service.EmailService emailService;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    // 短信服务配置
    private static final Integer DEFAULT_SMS_EXPIRATION_MINUTES = 5;

    @PostMapping("/login")
    public ResponseResult<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String account = loginRequest.getPhone();
            String password = loginRequest.getPassword();
            String smsCode = loginRequest.getCode(); // 短信验证码

            // 判断登录方式：短信验证码登录 or 密码登录
            if (smsCode != null && !smsCode.isEmpty()) {
                // ========== 短信验证码登录 ==========
                log.info("使用短信验证码登录，手机号: {}", account);

                // 验证短信验证码
                String storedCode = redisTemplate.opsForValue().get("sms-code:" + account);
                if (storedCode == null) {
                    return ResponseResult.fail("400", "验证码不存在或已过期，请重新获取");
                }

                // 严格验证验证码
                if (!smsCode.equals(storedCode)) {
                    log.warn("验证码错误！输入：{}，存储：{}", smsCode, storedCode);
                    return ResponseResult.fail("400", "验证码错误");
                }

                // 验证通过，删除已使用的验证码
                redisTemplate.delete("sms-code:" + account);

                // 查询用户
                User user = userService.lambdaQuery()
                        .eq(User::getPhone, account)
                        .one();

                if (user == null) {
                    return ResponseResult.fail("400", "用户不存在，请先注册");
                }

                // 生成token（使用JWT）
                String token = generateJWTToken(user);

                // 将头像转换为base64编码
                String avatarBase64 = convertAvatarToBase64(user.getAvatar());
                if (avatarBase64 != null) {
                    user.setAvatar(avatarBase64);
                }

                // 转换为UserDTO
                UserDTO userDTO = UserDTO.fromUser(user);

                // 构建响应
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("token", token);
                responseData.put("user", userDTO);
                responseData.put("userInfo", userDTO); // 兼容前端

                log.info("短信验证码登录成功: {}", account);
                return ResponseResult.success(responseData);

            } else if (password != null && !password.isEmpty()) {
                // ========== 密码登录 ==========
                log.info("使用密码登录，手机号: {}", account);

                // 验证图形验证码
                if (loginRequest.getCaptcha() == null || loginRequest.getCheckCodeKey() == null) {
                    return ResponseResult.fail("400", "验证码不能为空");
                }

                boolean isValidCaptcha = captchaUtil.validateCaptchaAndDelete(
                    loginRequest.getCaptcha(),
                    loginRequest.getCheckCodeKey()
                );
                if (!isValidCaptcha) {
                    return ResponseResult.fail("400", "验证码错误或已过期");
                }

                // 调用登录服务
                String token = userService.login(account, password);
                if (token != null) {
                    // 登录成功，查询用户详细信息
                    User user = userService.lambdaQuery()
                            .eq(User::getPhone, account)
                            .one();

                    if (user != null) {
                        // 将头像转换为base64编码
                        String avatarBase64 = convertAvatarToBase64(user.getAvatar());

                        if (avatarBase64 != null) {
                            user.setAvatar(avatarBase64);
                        }
                        log.info("login user entity: {}", user);

                        // 转换为UserDTO，隐藏敏感信息
                        UserDTO userDTO = UserDTO.fromUser(user);
                        log.info("login userDto : {}", userDTO);

                        // 构建包含token和用户信息的响应
                        Map<String, Object> responseData = new HashMap<>();
                        responseData.put("token", token);
                        responseData.put("user", userDTO);
                        responseData.put("userInfo", userDTO); // 兼容前端

                        return ResponseResult.success(responseData);
                    }
                }
                return ResponseResult.fail("500", "手机号或密码错误");

            } else {
                return ResponseResult.fail("400", "请使用密码或验证码登录");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "登录失败: " + e.getMessage());
        }
    }

    /**
     * 生成JWT Token
     */
    private String generateJWTToken(User user) {
        // 简单的JWT token生成逻辑
        // 实际项目中应该使用 io.jsonwebtoken 库
        try {
            long timestamp = System.currentTimeMillis();
            String tokenSignature = user.getPhone() + ":" + timestamp + ":jaseatschoice";
            String token = java.util.Base64.getEncoder().encodeToString(tokenSignature.getBytes());
            return token;
        } catch (Exception e) {
            log.error("生成token失败", e);
            return user.getPhone() + ":" + System.currentTimeMillis();
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseResult<?> getUserInfo(@PathVariable String userId) {
        log.info("Getting user info for userId: {}", userId);
        User user = userService.getById(userId);
        if (user != null) {
            // 隐藏敏感信息
            user.setPassword(null);
            log.info("user entity: {}", user);
            // 将头像转换为base64编码
            String avatarBase64 = convertAvatarToBase64(user.getAvatar());

            // 如果转换成功，使用base64，否则保留原始URL
            if (avatarBase64 != null) {
                // 将base64头像直接存入User对象
                user.setAvatar(avatarBase64);
            }

            // 转换为UserDTO
            com.xx.jaseatschoicejava.dto.UserDTO userDTO = com.xx.jaseatschoicejava.dto.UserDTO.fromUser(user);

            // 获取钱包信息并集成到UserDTO中
            try {
                com.xx.jaseatschoicejava.entity.Wallet wallet = walletService.getWalletByUserId(userId);
                if (wallet != null) {
                    userDTO.setWallet(com.xx.jaseatschoicejava.dto.WalletDTO.fromWallet(wallet));
                }
            } catch (Exception e) {
                log.warn("Failed to get wallet info for user {}: {}", userId, e.getMessage());
                // 钱包信息获取失败不影响用户信息的返回
            }

            // 返回包含钱包信息和base64头像的用户信息
            return ResponseResult.success(userDTO);
        }
        return ResponseResult.fail("404", "用户不存在");
    }

    /**
     * 将用户头像转换为base64编码
     * @param avatarUrl 用户头像的URL路径
     * @return base64编码的头像字符串，或null如果转换失败
     */
    private String convertAvatarToBase64(String avatarUrl) {
        if (avatarUrl == null) {
            return null;
        }

        try {
            // 拼接完整的图片路径
//            log.info("avatarUrl: {}", avatarUrl);
            String fullPath = fileUploadConfig.getUploadPath() + avatarUrl.substring(fileUploadConfig.getUrlPrefix().length());
//            log.info("fullPath: {}", fullPath);
            File avatarFile = new File(fullPath);
//            log.info("avatarFile: {}", avatarFile);
//                log.info("avatarFile exists: {}", avatarFile.exists());
            if (avatarFile.exists()) {
                byte[] imageBytes = Files.readAllBytes(avatarFile.toPath());
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (IOException e) {
            log.error("Failed to convert avatar to base64: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ResponseResult<?> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> updateData) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseResult.fail("404", "用户不存在");
            }

            // Update phone if provided
            if (updateData.containsKey("phone")) {
                String newPhone = (String) updateData.get("phone");
                // Validate new phone format
                if (!newPhone.matches("^1[3-9]\\d{9}$")) {
                    return ResponseResult.fail("400", "手机号格式错误");
                }

                // Check verification code if phone is changed
                if (!user.getPhone().equals(newPhone)) {
                    // Verify SMS verification code from updateData
                    String smsCode = (String) updateData.get("smsCode");
                    if (smsCode == null || smsCode.isEmpty()) {
                        return ResponseResult.fail("400", "短信验证码不能为空");
                    }

                    // Get the stored code from Redis
                    String storedCode = redisTemplate.opsForValue().get("sms-code:" + newPhone);
                    if (storedCode == null || !storedCode.equals(smsCode)) {
                        return ResponseResult.fail("400", "短信验证码错误或已过期");
                    }
                }

                user.setPhone(newPhone);
            }

            // Update email if provided
            if (updateData.containsKey("email")) {
                String newEmail = (String) updateData.get("email");
                // Validate new email format
                if (!newEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    return ResponseResult.fail("400", "邮箱格式错误");
                }

                // Check verification code if email is changed
                if (!user.getEmail().equals(newEmail)) {
                    // Verify email verification code from updateData
                    String emailCode = (String) updateData.get("emailCode");
                    if (emailCode == null || emailCode.isEmpty()) {
                        return ResponseResult.fail("400", "邮箱验证码不能为空");
                    }

                    // Get the stored code from Redis
                    String storedCode = redisTemplate.opsForValue().get("email-code:" + newEmail);
                    if (storedCode == null || !storedCode.equals(emailCode)) {
                        return ResponseResult.fail("400", "邮箱验证码错误或已过期");
                    }
                }

                user.setEmail(newEmail);
            }

            // Update the user
            boolean success = userService.updateById(user);
            if (success) {
                return ResponseResult.success("更新成功");
            }
            return ResponseResult.fail("500", "更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "更新失败");
        }
    }

    /**
     * 获取用户偏好设置
     */
    @GetMapping("/{userId}/preferences")
    public ResponseResult<?> getPreferences(@PathVariable String userId) {
        UserPreference userPreference = userPreferenceService.getByUserId(userId);
        if (userPreference != null) {
            return ResponseResult.success(userPreference);
        }
        return ResponseResult.fail("404", "用户偏好设置不存在");
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/send-sms-code")
    public ResponseResult<?> sendSmsCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseResult.fail("400", "手机号不能为空");
        }

        // Generate a random 6-digit code
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        try {
            // 使用阿里云短信服务发送验证码
            aliyunSMSService.sendSmsVerifyCode(phone, code);
            System.out.println("阿里云短信发送成功！手机号：" + phone + "，验证码：" + code);
        } catch (Exception e) {
            e.printStackTrace();
            // Return success even if SMS sending fails to prevent brute force attacks
        }

        // Store code in Redis with 5 minutes expiration
        redisTemplate.opsForValue().set("sms-code:" + phone, code, DEFAULT_SMS_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        return ResponseResult.success("手机验证码已发送");
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public ResponseResult<?> sendEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseResult.fail("400", "邮箱地址不能为空");
        }

        // 验证邮箱格式
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseResult.fail("400", "邮箱格式错误");
        }

        // 生成6位随机验证码
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        try {
            // 使用邮件服务发送验证码
            emailService.sendEmailVerifyCode(email, code);
            System.out.println("邮件发送成功！邮箱：" + email + "，验证码：" + code);
        } catch (Exception e) {
            e.printStackTrace();
            // 即使邮件发送失败也返回成功，防止暴力破解
        }

        // 将验证码存储到Redis，有效期5分钟
        redisTemplate.opsForValue().set("email-code:" + email, code, 5, TimeUnit.MINUTES);

        return ResponseResult.success("邮箱验证码已发送");
    }

    /**
     * 更新用户偏好设置
     */
    @PutMapping("/{userId}/preferences")
    public ResponseResult<?> updatePreferences(@PathVariable String userId, @RequestBody UserPreference preferences) {
        try {
            UserPreference existingPreference = userPreferenceService.getByUserId(userId);

            if (existingPreference != null) {
                // 更新现有偏好
                preferences.setId(existingPreference.getId());
                preferences.setUserId(userId);
            } else {
                // 新增偏好
                preferences.setUserId(userId);
            }

            boolean success = userPreferenceService.updatePreference(preferences);
            if (success) {
                // 清除用户偏好缓存（因为偏好设置已更新）
                userPreferenceCacheService.clearCache(userId);

                return ResponseResult.success("用户偏好设置已更新");
            }
            return ResponseResult.fail("500", "用户偏好设置更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "用户偏好设置更新失败");
        }
    }

    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param searchType 搜索类型：nickname(昵称), phone(手机号), email(邮箱)
     * @param userId 当前用户ID（用于过滤自己）
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    public ResponseResult<?> searchUsers(@RequestParam String keyword,
                                        @RequestParam(required = false) String searchType,
                                        @RequestParam(required = false) String userId) {
        try {
            log.info("搜索用户 - 关键词: {}, 搜索类型: {}, 当前用户ID: {}", keyword, searchType, userId);

            // 调用服务层搜索用户
            List<User> users = userService.searchUsers(keyword, searchType);

            // 如果提供了userId，过滤掉自己
            if (userId != null && !userId.trim().isEmpty()) {
                users = users.stream()
                        .filter(user -> !userId.equals(user.getUserId()))
                        .collect(Collectors.toList());
                log.info("过滤自己后的用户数量: {}", users.size());
            }

            return ResponseResult.success(users);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("用户搜索失败: {}", e.getMessage());
            return ResponseResult.fail("500", "用户搜索失败");
        }
    }

    /**
     * 上传用户头像 - 文件上传
     */
    @PostMapping("/{userId}/avatar")
    public ResponseResult<?> uploadAvatar(@PathVariable String userId,
                                         @RequestParam("file") MultipartFile file) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseResult.fail("404", "用户不存在");
            }

            // 上传图片（按用户ID分类存储）
            String fileName = FileUploadUtil.uploadImage(file, fileUploadConfig.getUploadPath(), user.getUserId());
            // 生成图片URL
            String avatarUrl = fileUploadConfig.getUrlPrefix() + fileName;
            // 更新用户头像
            user.setAvatar(avatarUrl);
            boolean success = userService.updateById(user);
            if (success) {
                // 将头像转换为base64编码
                String avatarBase64 = convertAvatarToBase64(avatarUrl);

                Map<String, String> result = new HashMap<>();
                result.put("avatarBase64", avatarBase64);

                // 通过WebSocket发送头像更新通知
                com.xx.jaseatschoicejava.netty.NettyChatHandler.sendAvatarUpdateNotification(String.valueOf(userId), avatarBase64);

                return ResponseResult.success(result);
            }
            return ResponseResult.fail("500", "头像上传失败");
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail("400", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "图片上传失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "系统错误");
        }
    }

    /**
     * 上传用户头像 - Base64格式
     */
    @PutMapping("/{userId}/avatar/base64")
    public ResponseResult<?> uploadAvatarBase64(@PathVariable String userId,
                                                @RequestBody Map<String, Object> base64Data) {
        log.info("Received base64 data from user {} : {}",userId, base64Data);
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseResult.fail("404", "用户不存在");
            }

            // 获取base64字符串
            String base64Str = (String) base64Data.get("avatarBase64");
            if (base64Str == null || base64Str.isEmpty()) {
                return ResponseResult.fail("400", "base64头像不能为空");
            }

            // 上传Base64图片（按用户ID分类存储）
            String fileName = FileUploadUtil.uploadBase64Image(base64Str, fileUploadConfig.getUploadPath(), user.getUserId());
            // 生成图片URL
            String avatarUrl = fileUploadConfig.getUrlPrefix() + fileName;
            // 更新用户头像
            user.setAvatar(avatarUrl);
//            log.info("Updating user {} with avatar URL: {} filename {} ", userId, avatarUrl, fileName);
            boolean success = userService.updateById(user);
            if (success) {
                // 将头像转换为base64编码
                String avatarBase64 = convertAvatarToBase64(avatarUrl);

                Map<String, String> result = new HashMap<>();
                result.put("avatarBase64", avatarBase64);

                // 通过WebSocket发送头像更新通知
                com.xx.jaseatschoicejava.netty.NettyChatHandler.sendAvatarUpdateNotification(user.getUserId(), avatarBase64);
//                log.info("Sending avatar update notification to user {} base64 {}", userId , avatarBase64);

                return ResponseResult.success(result);
            }
            return ResponseResult.fail("500", "头像上传失败");
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail("400", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "图片上传失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "系统错误");
        }
    }

    /**
     * 更新用户信息 - 包括头像
     */
    @PutMapping("/{userId}/info")
    public ResponseResult<?> updateUserInfo(@PathVariable String userId,
                                            @RequestBody Map<String, Object> updateData) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseResult.fail("404", "用户不存在");
            }

            // 更新基本信息
            if (updateData.containsKey("nickname")) {
                user.setNickname((String) updateData.get("nickname"));
            }
            if (updateData.containsKey("email")) {
                user.setEmail((String) updateData.get("email"));
            }
            if (updateData.containsKey("height")) {
                user.setHeight((Double) updateData.get("height"));
            }
            if (updateData.containsKey("weight")) {
                user.setWeight((Double) updateData.get("weight"));
            }
            if (updateData.containsKey("dietGoal")) {
                user.setDietGoal((String) updateData.get("dietGoal"));
            }
            if (updateData.containsKey("location")) {
                user.setLocation((String) updateData.get("location"));
            }

            boolean success = userService.updateById(user);
            if (success) {
                // 清除用户偏好缓存（因为基本信息或身体数据已更新）
                userPreferenceCacheService.clearCache(userId);

                // 返回更新后的用户信息
                User updatedUser = userService.getById(userId);
                updatedUser.setPassword(null); // 隐藏密码

                // 将头像转换为base64编码
                String avatarBase64 = convertAvatarToBase64(updatedUser.getAvatar());
                updatedUser.setAvatar(avatarBase64);

                return ResponseResult.success(updatedUser);
            }
            return ResponseResult.fail("500", "信息更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "系统错误");
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{userId}/password")
    public ResponseResult<?> updatePassword(@PathVariable String userId,
                                            @RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            // 参数验证
            if (oldPassword == null || oldPassword.isEmpty()) {
                return ResponseResult.fail("400", "旧密码不能为空");
            }
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseResult.fail("400", "新密码不能为空");
            }
            if (newPassword.length() < 6) {
                return ResponseResult.fail("400", "新密码长度不能少于6位");
            }

            // 调用服务层修改密码
            boolean success = userService.updatePassword(userId, oldPassword, newPassword);
            if (success) {
                return ResponseResult.success("密码修改成功");
            }
            return ResponseResult.fail("400", "旧密码错误");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "密码修改失败");
        }
    }

    /**
     * 提交用户反馈
     */
    @PostMapping("/feedback")
    public ResponseResult<?> submitFeedback(@RequestBody Map<String, String> feedbackData) {
        try {
            String userId = feedbackData.get("userId");
            String content = feedbackData.get("content");
            String contact = feedbackData.get("contact");

            // 参数验证
            if (content == null || content.isEmpty()) {
                return ResponseResult.fail("400", "反馈内容不能为空");
            }
            if (content.length() > 500) {
                return ResponseResult.fail("400", "反馈内容不能超过500字");
            }

            // 记录反馈（实际项目中应该保存到数据库）
            log.info("收到用户反馈 - 用户ID: {}, 联系方式: {}, 内容: {}", userId, contact, content);

            return ResponseResult.success("反馈已提交，感谢您的建议");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "反馈提交失败");
        }
    }
}

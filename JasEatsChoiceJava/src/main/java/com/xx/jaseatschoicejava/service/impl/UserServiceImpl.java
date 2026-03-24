package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import com.xx.jaseatschoicejava.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现
 *
 * 缓存策略：
 * - 用户基本信息：缓存30分钟
 * - 不缓存密码等敏感信息
 * - 更新用户信息时清除缓存
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String CACHE_NAME_USER_INFO = "user:info";
    private static final String CACHE_NAME_USER_PHONE = "user:info:phone";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 注册用户并对密码进行加密
     * @param user 用户对象
     * @return 注册成功返回true，否则返回false
     */
    @Override
    public boolean register(User user) {
        // 生成用户ID
        String userId = IdGenerator.toUserIdString(IdGenerator.generateId());
        user.setUserId(userId);
        // 对密码进行加密
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        return save(user);
    }

    /**
     * 用户登录，如果成功则返回JWT令牌
     * @param account 登录账号（手机号码）
     * @param password 密码
     * @return 登录成功返回JWT令牌，否则返回null
     */
    @Override
    public String login(String account, String password) {
        // 在我们的系统中，登录账号始终是手机号码
        User user = lambdaQuery()
                .eq(User::getPhone, account)
                .one();

        // 检查用户是否存在并验证密码是否正确
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 生成JWT令牌
            return jwtUtil.generateToken(user.getUserId(), user.getPhone());
        }
        return null;
    }

    /**
     * Search users by keyword
     * @param keyword Keyword to search
     * @return List of matching users
     */
    @Override
    public List<User> searchUsers(String keyword, String searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of(); // Return empty list if keyword is empty
        }

        String likeKeyword = "%" + keyword.trim() + "%";
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        // 根据搜索类型进行搜索
        if ("nickname".equals(searchType)) {
            // 仅搜索昵称
            queryWrapper.like("nickname", likeKeyword);
        } else if ("phone".equals(searchType)) {
            // 仅搜索手机号
            queryWrapper.like("phone", likeKeyword);
        } else if ("email".equals(searchType)) {
            // 仅搜索邮箱
            queryWrapper.like("email", likeKeyword);
        } else {
            // 默认同时搜索昵称、手机号和邮箱
            queryWrapper.like("nickname", likeKeyword)
                    .or()
                    .like("phone", likeKeyword)
                    .or()
                    .like("email", likeKeyword);
        }

        return getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 检查邮箱是否已被注册
     * @param email 邮箱地址
     * @return 如果邮箱已存在返回true，否则返回false
     */
    @Override
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return lambdaQuery()
                .eq(User::getEmail, email.trim())
                .count() > 0;
    }

    /**
     * 检查手机号是否已被注册
     * @param phone 手机号
     * @return 如果手机号已存在返回true，否则返回false
     */
    @Override
    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return lambdaQuery()
                .eq(User::getPhone, phone.trim())
                .count() > 0;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功返回true，否则返回false
     */
    @Override
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        // 获取用户信息
        User user = lambdaQuery()
                .eq(User::getUserId, userId)
                .one();

        if (user == null) {
            return false; // 用户不存在
        }

        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // 旧密码错误
        }

        // 加密新密码并更新
        String encryptedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedNewPassword);

        return updateById(user);
    }

    /**
     * 获取用户基本信息（带缓存）
     *
     * 注意：不包含密码等敏感信息
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @Cacheable(value = CACHE_NAME_USER_INFO, key = "#userId", unless = "#result == null")
    public User getUserInfoById(String userId) {
        log.debug("从数据库查询用户基本信息: userId={}", userId);
        User user = lambdaQuery()
                .eq(User::getUserId, userId)
                .one();

        // 清除密码字段
        if (user != null) {
            user.setPassword(null);
        }

        return user;
    }

    /**
     * 通过手机号获取用户基本信息（带缓存）
     *
     * @param phone 手机号
     * @return 用户基本信息
     */
    @Cacheable(value = CACHE_NAME_USER_PHONE, key = "#phone", unless = "#result == null")
    public User getUserInfoByPhone(String phone) {
        log.debug("从数据库查询用户基本信息: phone={}", phone);
        User user = lambdaQuery()
                .eq(User::getPhone, phone)
                .one();

        // 清除密码字段
        if (user != null) {
            user.setPassword(null);
        }

        return user;
    }

    /**
     * 更新用户信息并清除缓存
     *
     * @param user 用户信息
     * @return 是否成功
     */
    @CacheEvict(value = CACHE_NAME_USER_INFO, key = "#user.userId")
    public boolean updateUserInfo(User user) {
        log.debug("更新用户信息并清除缓存: userId={}", user.getUserId());
        return updateById(user);
    }

    /**
     * 清除用户缓存
     *
     * @param userId 用户ID
     */
    @CacheEvict(value = CACHE_NAME_USER_INFO, key = "#userId")
    public void evictUserCache(String userId) {
        log.debug("清除用户缓存: userId={}", userId);
    }

    /**
     * 清除手机号缓存
     *
     * @param phone 手机号
     */
    @CacheEvict(value = CACHE_NAME_USER_PHONE, key = "#phone")
    public void evictUserCacheByPhone(String phone) {
        log.debug("清除用户缓存: phone={}", phone);
    }
}

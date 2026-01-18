package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.constants.ApiConstants;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Merchant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.AnnouncementService;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.util.JwtUtil;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.entity.MessageRecord;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.ReviewService;
import com.xx.jaseatschoicejava.service.MessageRecordService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xx.jaseatschoicejava.entity.MerchantRegisterRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/v1/merchant")
public class MerchantController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDishService orderDishService;

    @Autowired
    private DishService dishService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MessageRecordService messageRecordService;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 商家注册
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private com.xx.jaseatschoicejava.util.CaptchaUtil captchaUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/register")
    public ResponseResult<?> register(@RequestBody MerchantRegisterRequest registerRequest, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            // 验证验证码
            if (registerRequest.getCaptcha() == null || registerRequest.getCaptchaKey() == null) {
                return ResponseResult.fail("400", "验证码不能为空");
            }

            // 使用验证码工具类验证验证码
            boolean isValidCaptcha = captchaUtil.validateCaptchaAndDelete(registerRequest.getCaptcha(), registerRequest.getCaptchaKey());
            if (!isValidCaptcha) {
                return ResponseResult.fail("400", "验证码错误或已过期");
            }

            // 创建Merchant对象并设置属性
            Merchant merchant = new Merchant();
            merchant.setName(registerRequest.getName());
            merchant.setBusinessLicense(registerRequest.getBusinessLicense());
            try {
                // 将经营范围数组转换为JsonNode
                ObjectMapper objectMapper = new ObjectMapper();
                merchant.setBusinessScope(objectMapper.valueToTree(registerRequest.getBusinessScope()));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseResult.fail("400", "经营范围格式错误");
            }
            merchant.setContactName(registerRequest.getContactName());
            merchant.setPhone(registerRequest.getPhone());
            merchant.setEmail(registerRequest.getEmail());

            Merchant    savedMerchant = merchantService.register(merchant);
            if (savedMerchant != null) {
                try {
                    // 从Authorization头中提取token
                    String token = authorizationHeader.replace("Bearer ", "").trim();
                    // 提取当前用户ID
                    String currentUserId = jwtUtil.extractUserId(token);

                    // 将merchantId存入user表
                    User user = userService.getById(currentUserId);
                    if (user != null) {
                        user.setMerchantId(savedMerchant.getId());
                        userService.updateById(user);
                    }
                } catch (Exception e) {
                    // 记录token处理异常，但不影响商家注册
                    e.printStackTrace();
                    logger.warn("处理用户-商家关联时失败: {}", e.getMessage());
                }

                // 可以选择在返回前清除密码等敏感信息
                savedMerchant.setPassword(null);
                return ResponseResult.success(savedMerchant);
            }
            return ResponseResult.fail("500", "注册失败");
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 处理手机号或邮箱重复的情况
            return ResponseResult.fail("400", "该手机号或邮箱已被注册，请更换其他账号");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "注册失败");
        }
    }

    /**
     * 获取商家列表
     */
    @GetMapping
    public ResponseResult<?> getMerchants(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            queryWrapper.eq(Merchant::getCategory, category);
        }
        if (keyword != null) {
            // 1. 首先查询包含关键词的菜品对应的商家ID
            LambdaQueryWrapper<Dish> dishQuery = new LambdaQueryWrapper<>();
            dishQuery.like(Dish::getName, keyword);
            List<String> merchantIdsWithMatchingDishes = dishService.list(dishQuery)
                    .stream()
                    .map(Dish::getMerchantId)
                    .map(String::valueOf)
                    .distinct()
                    .collect(Collectors.toList());

            // 2. 构建查询条件：商家名称包含关键词 或 商家有包含关键词的菜品
            queryWrapper.and(wrapper ->
                wrapper.like(Merchant::getName, keyword)
                .or()
                .in(!merchantIdsWithMatchingDishes.isEmpty(), Merchant::getId, merchantIdsWithMatchingDishes)
            );
        }
        // 只显示营业的商家
        queryWrapper.eq(Merchant::getStatus, true);
        List<Merchant> merchants = merchantService.list(queryWrapper);

        // 隐藏所有商家的敏感信息
        for (Merchant merchant : merchants) {
            merchant.setPassword(null);
        }

        return ResponseResult.success(merchants);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{merchantId}")
    public ResponseResult<?> getMerchantDetail(@PathVariable String merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant != null) {
            // 隐藏敏感信息
            merchant.setPassword(null);

            // 尝试手动解析 JSON 字段（如果 MyBatis-Plus 自动解析失败）
            try {
                // 我们可以通过直接查询 SQL 来获取原始的 JSON 字符串
                // 然后手动解析为 JsonNode
                String sql = "SELECT business_scope, business_hours, album FROM t_merchant WHERE id = ?";
                Object[] params = new Object[]{merchantId};

                // 使用 JdbcTemplate 直接查询
                Map<String, Object> resultMap = jdbcTemplate.queryForMap(sql, params);

                // 解析 business_scope
                if (resultMap.get("business_scope") != null) {
                    String businessScopeStr = resultMap.get("business_scope").toString();
                    JsonNode businessScopeJson = objectMapper.readTree(businessScopeStr);
                    merchant.setBusinessScope(businessScopeJson);
                }

                // 解析 business_hours
                if (resultMap.get("business_hours") != null) {
                    String businessHoursStr = resultMap.get("business_hours").toString();
                    JsonNode businessHoursJson = objectMapper.readTree(businessHoursStr);
                    merchant.setBusinessHours(businessHoursJson);
                }

                // 解析 album
                if (resultMap.get("album") != null) {
                    String albumStr = resultMap.get("album").toString();
                    JsonNode albumJson = objectMapper.readTree(albumStr);
                    merchant.setAlbum(albumJson);
                }

            } catch (Exception e) {
                logger.warn("手动解析 JSON 字段失败: {}", e.getMessage());
            }

            return ResponseResult.success(merchant);
        }
        return ResponseResult.fail("404", "商家不存在");
    }

    /**
     * 更新商家信息
     */
    @PutMapping("/{merchantId}")
    public ResponseResult<?> updateMerchant(@PathVariable String merchantId, @RequestBody Map<String, Object> requestBody) {
        // 参数验证
        if (merchantId == null || requestBody == null) {
            logger.error("更新商家信息失败：参数错误，merchantId={}, requestBody={}", merchantId, requestBody);
            return ResponseResult.fail("400", "参数错误");
        }

        logger.info("开始更新商家信息：merchantId={}, 请求数据={}", merchantId, requestBody);

        // 检查商家是否存在
        if (merchantService.getById(merchantId) == null) {
            logger.error("更新商家信息失败：商家不存在，merchantId={}", merchantId);
            return ResponseResult.fail("404", "商家不存在");
        }

        try {
            // 从数据库获取原商家信息
            Merchant merchant = merchantService.getById(merchantId);

            // 更新字段
            if (requestBody.containsKey("name")) {
                merchant.setName((String) requestBody.get("name"));
            }
            if (requestBody.containsKey("phone")) {
                merchant.setPhone((String) requestBody.get("phone"));
            }
            if (requestBody.containsKey("email")) {
                merchant.setEmail((String) requestBody.get("email"));
            }
            if (requestBody.containsKey("category")) {
                merchant.setCategory((String) requestBody.get("category"));
            }
            if (requestBody.containsKey("businessHours")) {
                Object businessHoursObj = requestBody.get("businessHours");
                if (businessHoursObj != null) {
                    try {
                        // 处理营业时间，支持字符串格式（如 "09:00-21:00"）或直接接收 JSON 对象/数组
                        JsonNode businessHoursJson;
                        if (businessHoursObj instanceof String) {
                            String businessHoursStr = (String) businessHoursObj;
                            // 如果是字符串格式 "HH:mm-HH:mm"，转换为 JSON 格式 {"start": "HH:mm", "end": "HH:mm"}
                            if (businessHoursStr.contains("-")) {
                                String[] timeRange = businessHoursStr.split("-");
                                if (timeRange.length == 2) {
                                    ObjectNode timeNode = objectMapper.createObjectNode();
                                    timeNode.put("start", timeRange[0].trim());
                                    timeNode.put("end", timeRange[1].trim());
                                    businessHoursJson = timeNode;
                                } else {
                                    // 格式不正确，设置为 null
                                    businessHoursJson = null;
                                }
                            } else {
                                // 如果不是范围格式，尝试解析为 JSON
                                try {
                                    businessHoursJson = objectMapper.readTree(businessHoursStr);
                                } catch (Exception e) {
                                    businessHoursJson = null;
                                }
                            }
                        } else {
                            // 如果是对象或数组类型，直接转换为 JsonNode
                            businessHoursJson = objectMapper.valueToTree(businessHoursObj);
                        }

                        merchant.setBusinessHours(businessHoursJson);
                    } catch (Exception e) {
                        logger.warn("解析营业时间失败: {}", e.getMessage());
                        merchant.setBusinessHours(null);
                    }
                }
            }
            if (requestBody.containsKey("averagePrice")) {
                merchant.setAveragePrice(new BigDecimal(requestBody.get("averagePrice").toString()));
            }
            if (requestBody.containsKey("status")) {
                merchant.setStatus((Boolean) requestBody.get("status"));
            }
            if (requestBody.containsKey("avatar")) {
                merchant.setAvatar((String) requestBody.get("avatar"));
            }
            if (requestBody.containsKey("businessScope")) {
                Object businessScopeObj = requestBody.get("businessScope");
                if (businessScopeObj != null) {
                    try {
                        JsonNode businessScopeJson;
                        if (businessScopeObj instanceof String) {
                            String businessScopeStr = (String) businessScopeObj;
                            // 尝试解析为 JSON
                            try {
                                businessScopeJson = objectMapper.readTree(businessScopeStr);
                            } catch (Exception e) {
                                // 如果解析失败，设置为 null
                                businessScopeJson = null;
                            }
                        } else {
                            // 如果是对象或数组类型，直接转换为 JsonNode
                            businessScopeJson = objectMapper.valueToTree(businessScopeObj);
                        }
                        merchant.setBusinessScope(businessScopeJson);
                    } catch (Exception e) {
                        logger.warn("解析经营范围失败: {}", e.getMessage());
                        merchant.setBusinessScope(null);
                    }
                }
            }

            // 处理地址字段 - 支持接收 areaAddress 和 detailAddress 或者直接接收 address
            if (requestBody.containsKey("areaAddress") && requestBody.containsKey("detailAddress")) {
                // 处理区域地址（数组转字符串）
                Object areaAddressObj = requestBody.get("areaAddress");
                String areaAddressStr = "";
                if (areaAddressObj instanceof List) {
                    List<?> areaAddressList = (List<?>) areaAddressObj;
                    areaAddressStr = String.join("/", areaAddressList.stream().map(Object::toString).toArray(String[]::new));
                } else if (areaAddressObj instanceof String) {
                    areaAddressStr = (String) areaAddressObj;
                }

                String detailAddress = (String) requestBody.get("detailAddress");
                merchant.setAddress(areaAddressStr + "/" + (detailAddress != null ? detailAddress.trim() : ""));
            } else if (requestBody.containsKey("address")) {
                // 保留对直接接收完整地址的支持
                merchant.setAddress((String) requestBody.get("address"));
            }

            // 处理经纬度信息
            if (requestBody.containsKey("latitude")) {
                Object latitudeObj = requestBody.get("latitude");
                if (latitudeObj != null && !(latitudeObj instanceof String && ((String) latitudeObj).trim().isEmpty())) {
                    try {
                        merchant.setLatitude(new BigDecimal(latitudeObj.toString()));
                    } catch (Exception e) {
                        logger.warn("无效的纬度值: {}", latitudeObj);
                    }
                }
            }
            if (requestBody.containsKey("longitude")) {
                Object longitudeObj = requestBody.get("longitude");
                if (longitudeObj != null && !(longitudeObj instanceof String && ((String) longitudeObj).trim().isEmpty())) {
                    try {
                        merchant.setLongitude(new BigDecimal(longitudeObj.toString()));
                    } catch (Exception e) {
                        logger.warn("无效的经度值: {}", longitudeObj);
                    }
                }
            }

            // 更新时间
            merchant.setUpdateTime(LocalDateTime.now());

            // 更新商家信息
            boolean success = merchantService.updateById(merchant);
            logger.info("更新商家信息结果：{}", success);

            logger.info("商家信息更新成功，merchantId={}", merchantId);
            return ResponseResult.success("更新成功");

        } catch (Exception e) {
            // 检查是否是JSON格式错误
            if (e.getMessage().contains("JSON") || e.getMessage().contains("json")) {
                logger.error("更新商家信息失败：JSON格式错误，requestBody={}", requestBody, e);
                return ResponseResult.fail("400", "参数错误：营业时间必须是有效的JSON格式");
            }
            // 其他类型的异常
            logger.error("更新商家信息失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "更新失败：数据格式不符合要求");
        }
    }

    /**
     * 更新商家状态
     */
    @PutMapping("/{merchantId}/status")
    public ResponseResult<?> updateMerchantStatus(@PathVariable String merchantId) {
        // 参数验证
        if (merchantId == null) {
            return ResponseResult.fail("400", "参数错误");
        }

        // 切换商家状态
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant != null) {
            merchant.setStatus(!merchant.getStatus());
            boolean success = merchantService.updateById(merchant);
            if (success) {
                return ResponseResult.success("状态更新成功");
            }
        }
        return ResponseResult.fail("500", "状态更新失败");
    }

    /**
     * 获取店铺相册
     */
    @GetMapping("/{merchantId}/album")
    public ResponseResult<?> getMerchantAlbum(@PathVariable String merchantId) {
        logger.info("获取店铺相册，merchantId={}", merchantId);

        try {
            // 使用原生SQL直接查询album字段（因为JacksonTypeHandler在读取时有问题）
            String albumJson = jdbcTemplate.queryForObject(
                "SELECT album FROM t_merchant WHERE id = ?",
                String.class,
                merchantId
            );
            logger.info("查询到的album JSON字符串：{}", albumJson);

            if (albumJson != null && !albumJson.isEmpty()) {
                // 手动解析JSON字符串
                JsonNode album = objectMapper.readTree(albumJson);
                logger.info("解析后的相册数据：{}", album);
                return ResponseResult.success(album);
            } else {
                // 如果相册为空，返回默认结构
                logger.info("相册为空，返回默认结构");
                ObjectNode defaultAlbum = objectMapper.createObjectNode();
                defaultAlbum.set("environment", objectMapper.createArrayNode());
                defaultAlbum.set("dishes", objectMapper.createArrayNode());
                return ResponseResult.success(defaultAlbum);
            }
        } catch (Exception e) {
            logger.error("获取相册数据失败：{}", e.getMessage(), e);
            // 出错时返回默认结构
            ObjectNode defaultAlbum = objectMapper.createObjectNode();
            defaultAlbum.set("environment", objectMapper.createArrayNode());
            defaultAlbum.set("dishes", objectMapper.createArrayNode());
            return ResponseResult.success(defaultAlbum);
        }
    }

    /**
     * 上传店铺相册照片
     */
    @PostMapping("/{merchantId}/album")
    public ResponseResult<?> uploadMerchantAlbum(@PathVariable String merchantId,
                                                @RequestParam("images") MultipartFile[] images,
                                                @RequestParam("albumType") String albumType) {
        // 从配置中获取上传目录
        String uploadDir = fileUploadConfig.getUploadPath();

        try {
            // 创建上传目录（如果不存在）
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 使用原生SQL直接查询album字段（避免JacksonTypeHandler的bug）
            String albumJson = jdbcTemplate.queryForObject(
                "SELECT album FROM t_merchant WHERE id = ?",
                String.class,
                merchantId
            );
            logger.info("上传前查询到的album JSON字符串：{}", albumJson);

            ObjectNode albumObject;
            if (albumJson != null && !albumJson.isEmpty()) {
                albumObject = (ObjectNode) objectMapper.readTree(albumJson);
            } else {
                // 如果相册为空，创建默认结构
                albumObject = objectMapper.createObjectNode();
                albumObject.set("environment", objectMapper.createArrayNode());
                albumObject.set("dishes", objectMapper.createArrayNode());
            }

            // 获取对应类型的相册数组
            ArrayNode albumArray = (ArrayNode) albumObject.get(albumType);

            // 实际上传图片并保存
            for (MultipartFile image : images) {
                // 生成唯一文件名，避免覆盖
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = System.currentTimeMillis() + "-" + UUID.randomUUID().toString() + fileExtension;

                // 保存文件到磁盘
                String filePath = uploadDir + uniqueFilename;
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(image.getBytes());
                }

                // 生成访问URL（这里使用本地服务器地址，实际项目中可以用域名或CDN地址）
                String imageUrl = ApiConstants.UPLOAD_URL_PREFIX + uniqueFilename;
                albumArray.add(imageUrl);
            }

            // 更新商家相册 - 使用原生SQL更新（因为JacksonTypeHandler在更新时有问题）
            String updatedAlbumJson = objectMapper.writeValueAsString(albumObject);
            logger.info("更新商家相册，merchantId={}, albumJson={}", merchantId, updatedAlbumJson);

            // 使用原生SQL更新album字段
            int updateCount = jdbcTemplate.update(
                "UPDATE t_merchant SET album = ?, update_time = NOW() WHERE id = ?",
                updatedAlbumJson,
                merchantId
            );
            logger.info("使用SQL更新商家相册结果：影响行数={}", updateCount);

            // 验证更新是否成功 - 重新查询
            Map<String, Object> result = jdbcTemplate.queryForMap(
                "SELECT album FROM t_merchant WHERE id = ?",
                merchantId
            );
            logger.info("验证更新后的相册数据: {}", result.get("album"));

            // 返回上传的图片URL
            return ResponseResult.success(albumArray);

        } catch (Exception e) {
            logger.error("上传相册照片失败：{}", e.getMessage(), e);
            return ResponseResult.fail("500", "上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除店铺相册照片
     */
    @DeleteMapping("/{merchantId}/album")
    public ResponseResult<?> deleteMerchantAlbum(@PathVariable String merchantId,
                                                @RequestParam("imageUrl") String imageUrl,
                                                @RequestParam("albumType") String albumType) {
        try {
            // 使用原生SQL直接查询album字段（避免JacksonTypeHandler的bug）
            String albumJson = jdbcTemplate.queryForObject(
                "SELECT album FROM t_merchant WHERE id = ?",
                String.class,
                merchantId
            );
            logger.info("删除前查询到的album JSON字符串：{}", albumJson);

            if (albumJson != null && !albumJson.isEmpty()) {
                ObjectNode albumObject = (ObjectNode) objectMapper.readTree(albumJson);
                ArrayNode albumArray = (ArrayNode) albumObject.get(albumType);

                // 查找并删除图片URL
                for (int i = 0; i < albumArray.size(); i++) {
                    if (albumArray.get(i).asText().equals(imageUrl)) {
                        albumArray.remove(i);
                        break;
                    }
                }

                // 更新商家相册 - 使用原生SQL更新
                String updatedAlbumJson = objectMapper.writeValueAsString(albumObject);
                logger.info("删除后更新商家相册，merchantId={}, albumJson={}", merchantId, updatedAlbumJson);

                // 使用原生SQL更新album字段
                int updateCount = jdbcTemplate.update(
                    "UPDATE t_merchant SET album = ?, update_time = NOW() WHERE id = ?",
                    updatedAlbumJson,
                    merchantId
                );
                logger.info("删除后使用SQL更新商家相册结果：影响行数={}", updateCount);

                // 从文件系统删除图片
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                String filePath = fileUploadConfig.getUploadPath() + fileName;
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                    logger.info("已删除文件: {}", filePath);
                }

                return ResponseResult.success("删除成功");
            }
            return ResponseResult.fail("404", "相册不存在或为空");
        } catch (Exception e) {
            logger.error("删除相册照片失败：{}", e.getMessage(), e);
            return ResponseResult.fail("500", "删除失败: " + e.getMessage());
        }
    }

    /**
     * 上传店铺头像
     */
    @PostMapping("/{merchantId}/avatar")
    public ResponseResult<?> uploadMerchantAvatar(@PathVariable String merchantId,
                                                 @RequestParam("avatar") MultipartFile avatar) {
        // 从配置中获取上传目录
        String uploadDir = fileUploadConfig.getUploadPath();

        try {
            // 创建上传目录（如果不存在）
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return ResponseResult.fail("404", "商家不存在");
            }

            // 生成唯一文件名
            String originalFilename = avatar.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = "avatar-" + merchantId + "-" + System.currentTimeMillis() + fileExtension;

            // 保存文件到磁盘
            String filePath = uploadDir + uniqueFilename;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(avatar.getBytes());
            }

            // 生成访问URL
            String avatarUrl = ApiConstants.UPLOAD_URL_PREFIX + uniqueFilename;

            // 更新商家头像
            merchant.setAvatar(avatarUrl);
            merchantService.updateById(merchant);

            return ResponseResult.success(avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日营业概览
     */
    @GetMapping("/{merchantId}/business-overview")
    public ResponseResult<?> getBusinessOverview(@PathVariable String merchantId) {
        try {
            // 从数据库获取真实数据
            Map<String, Object> overview = new HashMap<>();

            // 获取当前日期
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);

            // 获取昨天的日期
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime startOfYesterday = yesterday.atStartOfDay();
            LocalDateTime endOfYesterday = yesterday.plusDays(1).atStartOfDay().minusSeconds(1);

            // 查询今日订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getMerchantId, merchantId)
                       .ge(Order::getCreateTime, startOfDay)
                       .le(Order::getCreateTime, endOfDay);

            // 获取订单列表
            List<Order> orders = orderService.list(queryWrapper);

            // 计算今日营业额（排除已取消的订单）
            BigDecimal totalSales = orders.stream()
                    .filter(order -> order.getStatus() != 6) // status 6 是已取消
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 计算今日订单数（排除已取消的订单）
            long totalOrders = orders.stream()
                    .filter(order -> order.getStatus() != 6) // status 6 是已取消
                    .count();

            // 查询昨日订单
            LambdaQueryWrapper<Order> yesterdayQueryWrapper = new LambdaQueryWrapper<>();
            yesterdayQueryWrapper.eq(Order::getMerchantId, merchantId)
                                .ge(Order::getCreateTime, startOfYesterday)
                                .le(Order::getCreateTime, endOfYesterday);

            // 获取昨日订单列表
            List<Order> yesterdayOrders = orderService.list(yesterdayQueryWrapper);

            // 计算昨日营业额（排除已取消的订单）
            BigDecimal yesterdaySales = yesterdayOrders.stream()
                    .filter(order -> order.getStatus() != 6)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 计算昨日订单数（排除已取消的订单）
            long yesterdayOrdersCount = yesterdayOrders.stream()
                    .filter(order -> order.getStatus() != 6)
                    .count();

            // 计算趋势百分比和方向
            String salesTrend = calculateTrend(totalSales, yesterdaySales);
            String ordersTrend = calculateTrend(
                BigDecimal.valueOf(totalOrders),
                BigDecimal.valueOf(yesterdayOrdersCount)
            );

            // 设置结果
            overview.put("sales", totalSales.doubleValue());       // 营业额
            overview.put("orders", totalOrders);                    // 订单数
            overview.put("salesTrend", salesTrend);                 // 营业额趋势
            overview.put("ordersTrend", ordersTrend);               // 订单数趋势

            // 新增评价数量（使用真实数据，过去7天内的评价）
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            LambdaQueryWrapper<Review> reviewQueryWrapper = new LambdaQueryWrapper<>();
            reviewQueryWrapper.eq(Review::getMerchantId, merchantId)
                           .eq(Review::getStatus, 0) // 正常状态
                           .ge(Review::getCreateTime, oneWeekAgo);
            long newCommentsCount = reviewService.count(reviewQueryWrapper);
            overview.put("newComments", newCommentsCount);  // 新增评价

            // 上周同期的评价数量（用于趋势对比）
            LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
            LambdaQueryWrapper<Review> lastWeekReviewQueryWrapper = new LambdaQueryWrapper<>();
            lastWeekReviewQueryWrapper.eq(Review::getMerchantId, merchantId)
                                    .eq(Review::getStatus, 0)
                                    .ge(Review::getCreateTime, twoWeeksAgo)
                                    .le(Review::getCreateTime, oneWeekAgo);
            long lastWeekCommentsCount = reviewService.count(lastWeekReviewQueryWrapper);

            String commentsTrend = calculateTrend(
                BigDecimal.valueOf(newCommentsCount),
                BigDecimal.valueOf(lastWeekCommentsCount)
            );
            overview.put("commentsTrend", commentsTrend);  // 新增评价趋势

            // 未读消息数量（查询接收者为该商家且未读的消息）
            LambdaQueryWrapper<MessageRecord> messageQueryWrapper = new LambdaQueryWrapper<>();
            messageQueryWrapper.eq(MessageRecord::getReceiverId, merchantId)
                             .eq(MessageRecord::getReadStatus, 0); // 0表示未读
            long unreadMessagesCount = messageRecordService.count(messageQueryWrapper);
            overview.put("unreadMessages", unreadMessagesCount);  // 未读消息

            // 未读消息趋势（简单处理：有消息显示上升，无消息显示持平）
            String messagesTrend = unreadMessagesCount > 0 ? "↑ 100%" : "→ 0%";
            overview.put("messagesTrend", messagesTrend);  // 未读消息趋势

            return ResponseResult.success(overview);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "获取营业概览失败");
        }
    }

    /**
     * 计算趋势百分比和方向
     * @param current 当前值
     * @param previous 对比值
     * @return 趋势字符串，如 "↑ 12.5%"、"↓ 2.1%"、"→ 0%"
     */
    private String calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            // 如果昨天的数据为0，无法计算百分比
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return "→ 0%";  // 今天也是0
            } else {
                return "↑ 100%";  // 昨天是0，今天有数据，显示上升100%
            }
        }

        // 计算变化百分比
        BigDecimal change = current.subtract(previous);
        BigDecimal percentage = change.divide(previous, 4, BigDecimal.ROUND_HALF_UP)
                                       .multiply(BigDecimal.valueOf(100));

        // 格式化百分比，保留1位小数
        double percentageValue = percentage.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

        // 判断趋势方向
        if (percentageValue > 0) {
            return "↑ " + Math.abs(percentageValue) + "%";
        } else if (percentageValue < 0) {
            return "↓ " + Math.abs(percentageValue) + "%";
        } else {
            return "→ 0%";
        }
    }

    /**
     * 获取经营统计数据
     */
    @GetMapping("/{merchantId}/statistics")
    public ResponseResult<?> getStatistics(@PathVariable String merchantId, @RequestParam String timeRange) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startTime = null;
            LocalDateTime endTime = LocalDate.now().atStartOfDay().plusDays(1).minusSeconds(1);

            // 根据时间范围确定查询时间
            switch (timeRange) {
                case "today":
                    startTime = today.atStartOfDay();
                    break;
                case "yesterday":
                    startTime = today.minusDays(1).atStartOfDay();
                    endTime = today.atStartOfDay().minusSeconds(1);
                    break;
                case "week":
                    startTime = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
                    break;
                case "month":
                    startTime = today.withDayOfMonth(1).atStartOfDay();
                    break;
            }

            // 查询订单数据
            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Order::getMerchantId, merchantId)
                      .ge(Order::getCreateTime, startTime)
                      .le(Order::getCreateTime, endTime)
                      .ne(Order::getStatus, 6); // 排除已取消订单

            List<Order> orders = orderService.list(orderQuery);

            // 计算基本统计数据
            Map<String, Object> basicStats = new HashMap<>();
            long totalOrders = orders.size();
            double totalAmount = orders.stream()
                                      .map(Order::getTotalAmount)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add)
                                      .doubleValue();
            double avgAmount = totalOrders > 0 ? totalAmount / totalOrders : 0;
            long newCustomers = orders.stream()
                                      .map(Order::getUserId)
                                      .distinct()
                                      .count();

            basicStats.put("orders", totalOrders);
            basicStats.put("totalAmount", totalAmount);
            basicStats.put("avgAmount", avgAmount);
            basicStats.put("newCustomers", newCustomers);

            // 准备响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("basicStats", basicStats);

            // 计算订单趋势数据
            List<Map<String, Object>> orderTrend = new ArrayList<>();

            // 根据时间范围计算不同的时间间隔
            if ("today".equals(timeRange) || "yesterday".equals(timeRange)) {
                // 按小时统计
                for (int i = 0; i < 24; i++) {
                    String time = String.format("%02d:00", i);
                    LocalDateTime hourStart = startTime.withHour(i).withMinute(0).withSecond(0);
                    LocalDateTime hourEnd = startTime.withHour(i).withMinute(59).withSecond(59);

                    // 处理昨天的情况
                    final LocalDateTime finalStart;
                    final LocalDateTime finalEnd;
                    if ("yesterday".equals(timeRange)) {
                        finalStart = hourStart.minusDays(1);
                        finalEnd = hourEnd.minusDays(1);
                    } else {
                        finalStart = hourStart;
                        finalEnd = hourEnd;
                    }

                    long count = orders.stream()
                                      .filter(order -> order.getCreateTime().isAfter(finalStart) && order.getCreateTime().isBefore(finalEnd))
                                      .count();

                    Map<String, Object> trendItem = new HashMap<>();
                    trendItem.put("time", time);
                    trendItem.put("orders", count);
                    orderTrend.add(trendItem);
                }
            } else if ("week".equals(timeRange)) {
                // 按星期统计
                String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

                for (int i = 0; i < 7; i++) {
                    LocalDateTime dayStart = startTime.plusDays(i);
                    LocalDateTime dayEnd = startTime.plusDays(i).plusDays(1).minusSeconds(1);

                    long count = orders.stream()
                                      .filter(order -> order.getCreateTime().isAfter(dayStart) && order.getCreateTime().isBefore(dayEnd))
                                      .count();

                    Map<String, Object> trendItem = new HashMap<>();
                    trendItem.put("time", weekdays[i]);
                    trendItem.put("orders", count);
                    orderTrend.add(trendItem);
                }
            } else if ("month".equals(timeRange)) {
                // 按月统计
                String[] months = {"1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日",
                                 "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日",
                                 "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日", "30日", "31日"};

                // 统计前5天
                for (int i = 0; i < 5; i++) {
                    LocalDateTime dayStart = startTime.plusDays(i);
                    LocalDateTime dayEnd = startTime.plusDays(i).plusDays(1).minusSeconds(1);

                    long count = orders.stream()
                                      .filter(order -> order.getCreateTime().isAfter(dayStart) && order.getCreateTime().isBefore(dayEnd))
                                      .count();

                    Map<String, Object> trendItem = new HashMap<>();
                    trendItem.put("time", months[i]);
                    trendItem.put("orders", count);
                    orderTrend.add(trendItem);
                }
            }

            response.put("orderTrend", orderTrend);

            // 计算菜品销量排行数据
            // 先获取所有订单ID
            List<String> orderIds = orders.stream()
                                      .map(Order::getId)
                                      .collect(Collectors.toList());

            List<Map<String, Object>> dishSalesRank = new ArrayList<>();

            if (!orderIds.isEmpty()) {
                // 查询所有订单菜品
                LambdaQueryWrapper<OrderDish> orderDishQuery = new LambdaQueryWrapper<>();
                orderDishQuery.in(OrderDish::getOrderId, orderIds);

                List<OrderDish> orderDishes = orderDishService.list(orderDishQuery);

                // 按菜品ID分组，统计销量
                Map<String, Integer> dishSalesMap = orderDishes.stream()
                                                            .collect(Collectors.groupingBy(OrderDish::getDishId,
                                                                                         Collectors.summingInt(OrderDish::getQuantity)));

                // 获取菜品信息并计算销售额
                for (Map.Entry<String, Integer> entry : dishSalesMap.entrySet()) {
                    String dishId = entry.getKey();
                    int sales = entry.getValue();

                    Dish dish = dishService.getById(dishId);
                    if (dish != null) {
                        Map<String, Object> dishSales = new HashMap<>();
                        dishSales.put("name", dish.getName());
                        dishSales.put("sales", sales);
                        dishSales.put("revenue", dish.getPrice().multiply(new BigDecimal(sales)).doubleValue());
                        dishSalesRank.add(dishSales);
                    }
                }

                // 按销量排序
                dishSalesRank.sort((a, b) -> Integer.compare((Integer) b.get("sales"), (Integer) a.get("sales")));

                // 只取前5名
                if (dishSalesRank.size() > 5) {
                    dishSalesRank = dishSalesRank.subList(0, 5);
                }
            }

            response.put("dishSalesRank", dishSalesRank);

            return ResponseResult.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "获取统计数据失败");
        }
    }

    /**
     * 获取商家的公告列表
     */
    @GetMapping("/{merchantId}/announcements")
    public ResponseResult<?> getAnnouncements(@PathVariable String merchantId) {
        try {
            LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Announcement::getMerchantId, merchantId)
                    .orderByDesc(Announcement::getCreateTime);
            List<Announcement> announcements = announcementService.list(queryWrapper);
            return ResponseResult.success(announcements);
        } catch (Exception e) {
            logger.error("获取公告列表失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "获取公告列表失败");
        }
    }

    /**
     * 添加公告
     */
    @PostMapping("/{merchantId}/announcements")
    public ResponseResult<?> addAnnouncement(@PathVariable String merchantId, @RequestBody Announcement announcement) {
        try {
            announcement.setMerchantId(merchantId);
            boolean success = announcementService.save(announcement);
            if (success) {
                // 重新查询数据库以获取完整的字段值（包括自动填充的createTime和updateTime）
                Announcement savedAnnouncement = announcementService.getById(announcement.getId());
                return ResponseResult.success(savedAnnouncement, "公告添加成功");
            }
            return ResponseResult.fail("500", "公告添加失败");
        } catch (Exception e) {
            logger.error("添加公告失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "公告添加失败");
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{merchantId}/announcements/{announcementId}")
    public ResponseResult<?> updateAnnouncement(
            @PathVariable String merchantId,
            @PathVariable String announcementId,
            @RequestBody Announcement announcement) {
        try {
            // 验证公告属于该商家
            Announcement existingAnnouncement = announcementService.getById(announcementId);
            if (existingAnnouncement == null || !existingAnnouncement.getMerchantId().equals(merchantId)) {
                return ResponseResult.fail("404", "公告不存在");
            }

            // 确保ID一致
            announcement.setId(announcementId);
            announcement.setMerchantId(merchantId);

            boolean success = announcementService.updateById(announcement);
            if (success) {
                return ResponseResult.success(announcement, "公告更新成功");
            }
            return ResponseResult.fail("500", "公告更新失败");
        } catch (Exception e) {
            logger.error("更新公告失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "公告更新失败");
        }
    }

    /**
     * 切换公告状态
     */
    @PutMapping("/{merchantId}/announcements/{announcementId}/status")
    public ResponseResult<?> toggleAnnouncementStatus(
            @PathVariable String merchantId,
            @PathVariable String announcementId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");

            // 验证公告属于该商家
            Announcement announcement = announcementService.getById(announcementId);
            if (announcement == null || !announcement.getMerchantId().equals(Long.valueOf(merchantId))) {
                return ResponseResult.fail("404", "公告不存在");
            }

            // 更新状态
            announcement.setStatus(status);
            boolean success = announcementService.updateById(announcement);
            if (success) {
                return ResponseResult.success(announcement, "状态更新成功");
            }
            return ResponseResult.fail("500", "状态更新失败");
        } catch (Exception e) {
            logger.error("切换公告状态失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "状态更新失败");
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{merchantId}/announcements/{announcementId}")
    public ResponseResult<?> deleteAnnouncement(
            @PathVariable String merchantId,
            @PathVariable String announcementId) {
        try {
            // 验证公告属于该商家
            Announcement announcement = announcementService.getById(announcementId);
            if (announcement == null || !announcement.getMerchantId().equals(Long.valueOf(merchantId))) {
                return ResponseResult.fail("404", "公告不存在");
            }

            boolean success = announcementService.removeById(announcementId);
            if (success) {
                return ResponseResult.success(null, "公告删除成功");
            }
            return ResponseResult.fail("500", "公告删除失败");
        } catch (Exception e) {
            logger.error("删除公告失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "公告删除失败");
        }
    }
}

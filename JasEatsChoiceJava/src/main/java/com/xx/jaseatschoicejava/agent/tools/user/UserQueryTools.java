package com.xx.jaseatschoicejava.agent.tools.user;

import com.xx.jaseatschoicejava.agent.dto.UserBasicInfo;
import com.xx.jaseatschoicejava.agent.dto.UserProfile;
import com.xx.jaseatschoicejava.agent.dto.UserStatistics;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 用户查询工具类
 *
 * 为Agent提供用户信息的查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class UserQueryTools {

    @Resource
    private UserService userService;

    /**
     * 获取用户基本信息
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @Tool("""
        获取用户的基本信息，包括：
        - 用户ID
        - 昵称
        - 手机号
        - 邮箱
        - 身高体重
        - 饮食目标
        - 偏好标签
        - 过敏信息

        **何时使用：**
        - 需要了解用户基本信息
        - 验证用户身份
        - 个性化推荐时获取用户特征

        **参数：** userId - 用户ID

        **返回：** 用户基本信息对象
        """)
    public UserBasicInfo getUserInfo(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询用户基本信息，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return UserBasicInfo.builder()
                        .userId(userId)
                        .nickname("未知用户")
                        .exists(false)
                        .build();
            }

            UserBasicInfo info = UserBasicInfo.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .phone(user.getPhone())
                    .email(user.getEmail())
                    .memberLevel("普通会员") // 可从user对象获取
                    .status("正常")
                    .exists(true)
                    .build();

            log.info("✅ [Tool] 查询用户信息成功: {}", info.getNickname());
            return info;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询用户信息失败，userId: {}", userId, e);
            return UserBasicInfo.builder()
                    .userId(userId)
                    .nickname("查询失败")
                    .exists(false)
                    .build();
        }
    }

    /**
     * 获取用户详细资料
     *
     * @param userId 用户ID
     * @return 用户详细资料
     */
    @Tool("""
        获取用户的详细资料，包括：
        - 基本信息（昵称、手机号等）
        - 个人资料（身高、体重、性别等）
        - 健康目标（dietGoal）
        - 饮食偏好（preferTags）
        - 过敏信息（allergies）

        **何时使用：**
        - 需要全面了解用户
        - 个性化推荐
        - 制定饮食计划
        - 健康建议

        **参数：** userId - 用户ID

        **返回：** 用户详细资料对象
        """)
    public UserProfile getUserProfile(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询用户详细资料，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return UserProfile.builder()
                        .userId(userId)
                        .exists(false)
                        .build();
            }

            UserProfile profile = UserProfile.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .phone(user.getPhone())
                    .email(user.getEmail())
                    .height(user.getHeight())
                    .weight(user.getWeight())
                    .gender(user.getGender())
                    .dietGoal(user.getDietGoal())
                    .preferTags(user.getPreferTags())
                    .allergies(user.getAllergies())
                    .orderCount(0) // 从订单表统计
                    .totalSpending(0.0) // 从订单表统计
                    .exists(true)
                    .build();

            log.info("✅ [Tool] 查询用户详细资料成功: {}", profile.getNickname());
            return profile;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询用户详细资料失败，userId: {}", userId, e);
            return UserProfile.builder()
                    .userId(userId)
                    .exists(false)
                    .build();
        }
    }

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @Tool("""
        批量获取多个用户的基本信息

        **何时使用：**
        - 需要查询多个用户
        - 用户列表展示
        - 批量操作

        **参数：** userIds - 用户ID列表

        **返回：** 用户信息列表
        """)
    public List<UserBasicInfo> batchGetUsers(
        @P("用户ID列表") List<String> userIds
    ) {
        log.info("🔍 [Tool] 批量查询用户信息，数量: {}", userIds.size());

        try {
            List<UserBasicInfo> results = userIds.stream()
                .map(userId -> getUserInfo(userId))
                .filter(info -> info.getExists())
                .toList();

            log.info("✅ [Tool] 批量查询成功，返回: {} 个用户", results.size());
            return results;

        } catch (Exception e) {
            log.error("❌ [Tool] 批量查询用户信息失败", e);
            return List.of();
        }
    }

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    @Tool("""
        检查用户是否存在

        **何时使用：**
        - 验证用户身份
        - 注册前检查
        - 登录验证

        **参数：** userId - 用户ID

        **返回：** true-存在，false-不存在
        """)
    public boolean checkUserExists(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 检查用户是否存在，userId: {}", userId);

        try {
            User user = userService.getById(userId);
            boolean exists = user != null;
            log.info("✅ [Tool] 用户存在检查结果: {}", exists);
            return exists;

        } catch (Exception e) {
            log.error("❌ [Tool] 用户存在检查失败", e);
            return false;
        }
    }

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @Tool("""
        获取用户的统计信息，包括：
        - 总订单数
        - 总消费金额
        - 平均订单金额
        - 最近订购时间

        **何时使用：**
        - 用户画像分析
        - 推荐策略调整
        - 用户价值评估

        **参数：** userId - 用户ID

        **返回：** 用户统计信息
        """)
    public UserStatistics getUserStatistics(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询用户统计信息，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return UserStatistics.builder()
                        .userId(userId)
                        .totalOrders(0)
                        .totalSpending(0.0)
                        .build();
            }

            // TODO: 从订单服务统计实际数据
            UserStatistics stats = UserStatistics.builder()
                    .userId(userId)
                    .totalOrders(0) // 从Order表统计
                    .totalSpending(0.0) // 从Order表统计
                    .averageOrderAmount(0.0)
                    .lastOrderTime("暂无")
                    .memberLevel("普通会员")
                    .build();

            log.info("✅ [Tool] 查询用户统计成功");
            return stats;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询用户统计失败", e);
            return UserStatistics.builder()
                    .userId(userId)
                    .build();
        }
    }

    /**
     * 获取用户偏好标签
     *
     * @param userId 用户ID
     * @return 偏好标签（JSON字符串）
     */
    @Tool("""
        获取用户的饮食偏好标签

        **偏好标签包括：**
        - 口味偏好（辣度、甜度等）
        - 菜系偏好（川菜、粤菜等）
        - 素食/荤食
        - 价格区间
        - 营养需求（低卡、低脂等）

        **何时使用：**
        - 个性化推荐
        - 菜品筛选

        **参数：** userId - 用户ID

        **返回：** 偏好标签JSON字符串
        """)
    public String getUserPreferenceTags(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询用户偏好标签，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null || user.getPreferTags() == null) {
                return "{}";
            }

            return user.getPreferTags().toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询用户偏好标签失败", e);
            return "{}";
        }
    }

    /**
     * 获取用户过敏信息
     *
     * @param userId 用户ID
     * @return 过敏信息（JSON字符串）
     */
    @Tool("""
        获取用户的过敏食材信息

        **何时使用：**
        - 推荐菜品时过滤
        - 检查食物安全性

        **参数：** userId - 用户ID

        **返回：** 过敏信息JSON字符串
        """)
    public String getUserAllergies(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询用户过敏信息，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null || user.getAllergies() == null) {
                return "[]";
            }

            return user.getAllergies().toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询用户过敏信息失败", e);
            return "[]";
        }
    }
}

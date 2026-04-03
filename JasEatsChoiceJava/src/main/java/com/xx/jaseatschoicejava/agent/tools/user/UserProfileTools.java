package com.xx.jaseatschoicejava.agent.tools.user;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agentic.scope.AgenticScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.Period;

/**
 * 用户资料完善工具类
 *
 * 为Agent提供用户资料管理功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class UserProfileTools {

    @Resource
    private UserService userService;

    /**
     * 获取用户完整资料
     *
     * @param userId 用户ID
     * @return 用户完整资料
     */
    @Tool("""
        获取用户的完整资料信息

        **资料包含：**
        - 基本信息（昵称、手机号、性别）
        - 身体数据（身高、体重、生日）
        - 健康目标（减肥/增肌/保持/增重）
        - 饮食偏好（口味、菜系、忌口）
        - 统计数据（会员等级、积分）

        **何时使用：**
        - 需要全面了解用户
        - 个性化推荐
        - 制定饮食计划

        **无需参数**，userId自动从上下文获取

        **返回：** 用户完整资料
        """)
    public String getCompleteProfile(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 获取用户完整资料，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("👤 用户完整资料\n\n");

            // 基本信息
            sb.append("📋 **基本信息**\n");
            sb.append(String.format("  • 昵称：%s\n", user.getNickname() != null ? user.getNickname() : "未设置"));
            sb.append(String.format("  • 性别：%s\n", user.getGender() != null ? user.getGender() : "未设置"));
            sb.append(String.format("  • 手机号：%s\n\n", user.getPhone() != null ? maskPhone(user.getPhone()) : "未绑定"));

            // 身体数据
            sb.append("💪 **身体数据**\n");
            sb.append(String.format("  • 身高：%.1f cm\n", user.getHeight() != null ? user.getHeight() : 0));
            sb.append(String.format("  • 体重：%.1f kg\n", user.getWeight() != null ? user.getWeight() : 0));
            sb.append(String.format("  • BMI：%.1f\n\n", calculateBMI(user)));

            // 健康目标
            sb.append("🎯 **健康目标**\n");
            sb.append(String.format("  • 目标：%s\n\n", user.getDietGoal() != null ? user.getDietGoal() : "未设置"));

            // 饮食偏好
            sb.append("🍽️ **饮食偏好**\n");
            if (user.getPreferTags() != null && !user.getPreferTags().isEmpty()) {
                sb.append(String.format("  • 偏好标签：%s\n\n", user.getPreferTags().toString()));
            } else {
                sb.append("  • 暂无偏好设置\n\n");
            }

            // 过敏信息
            if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
                sb.append("⚠️ **过敏信息**\n");
                sb.append(String.format("  • 忌口：%s\n\n", user.getAllergies().toString()));
            }

            log.info("✅ [Tool] 获取用户资料成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取用户资料失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 更新用户基本信息
     *
     * @param userId 用户ID
     * @param nickname 昵称（可选）
     * @param gender 性别（可选）
     * @param phone 手机号（可选）
     * @return 更新结果
     */
    @Tool("""
        更新用户的基本信息

        **可更新字段：**
        - 昵称
        - 性别（男/女）
        - 手机号

        **何时使用：**
        - 用户修改个人资料
        - 完善用户信息

        **参数：**
        - **无需参数**，userId自动从上下文获取
        - nickname - 昵称（可选）
        - gender - 性别（可选）
        - phone - 手机号（可选）

        **返回：** 更新结果
        """)
    public String updateBasicInfo(
        AgenticScope scope,
        @P("昵称（可选）") String nickname,
        @P("性别：男/女（可选）") String gender,
        @P("手机号（可选）") String phone
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 更新用户基本信息，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            int updateCount = 0;
            StringBuilder updates = new StringBuilder();

            if (nickname != null && !nickname.isEmpty()) {
                user.setNickname(nickname);
                updates.append(String.format("  • 昵称：%s\n", nickname));
                updateCount++;
            }

            if (gender != null && (gender.equals("男") || gender.equals("女"))) {
                user.setGender(gender);
                updates.append(String.format("  • 性别：%s\n", gender));
                updateCount++;
            }

            if (phone != null && !phone.isEmpty()) {
                user.setPhone(phone);
                updates.append(String.format("  • 手机号：%s\n", maskPhone(phone)));
                updateCount++;
            }

            if (updateCount == 0) {
                return "⚠️ 没有提供需要更新的信息";
            }

            userService.updateById(user);

            StringBuilder result = new StringBuilder();
            result.append("✅ 个人信息更新成功！\n\n");
            result.append("**已更新：**\n");
            result.append(updates.toString());
            result.append("\n💡 信息已保存");

            log.info("✅ [Tool] 更新用户信息成功，更新项: {}", updateCount);
            return result.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 更新用户信息失败", e);
            return "❌ 更新失败：" + e.getMessage();
        }
    }

    /**
     * 更新身体数据
     *
     * @param userId 用户ID
     * @param height 身高（cm，可选）
     * @param weight 体重（kg，可选）
     * @return 更新结果
     */
    @Tool("""
        更新用户的身体数据

        **可更新字段：**
        - 身高（cm）
        - 体重（kg）

        **何时使用：**
        - 用户记录身体数据
        - 追踪健康变化
        - 计算热量目标

        **参数：**
        - **无需参数**，userId自动从上下文获取
        - height - 身高cm（可选）
        - weight - 体重kg（可选）

        **返回：** 更新结果和BMI分析
        """)
    public String updateBodyData(
        AgenticScope scope,
        @P("身高cm（可选）") Double height,
        @P("体重kg（可选）") Double weight
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 更新用户身体数据，userId: {}, height: {}, weight: {}", userId, height, weight);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            if (height == null && weight == null) {
                return "⚠️ 请提供身高或体重数据";
            }

            StringBuilder updates = new StringBuilder();

            if (height != null && height > 0) {
                user.setHeight(height);
                updates.append(String.format("  • 身高：%.1f cm\n", height));
            }

            if (weight != null && weight > 0) {
                Double oldWeight = user.getWeight();
                user.setWeight(weight);
                if (oldWeight != null && oldWeight > 0) {
                    double diff = weight - oldWeight;
                    updates.append(String.format("  • 体重：%.1f kg (%.1f kg)\n", weight, diff));
                } else {
                    updates.append(String.format("  • 体重：%.1f kg\n", weight));
                }
            }

            userService.updateById(user);

            // 计算BMI
            double bmi = calculateBMI(user);
            String bmiStatus = getBMIStatus(bmi);

            StringBuilder result = new StringBuilder();
            result.append("✅ 身体数据更新成功！\n\n");
            result.append("**已更新：**\n");
            result.append(updates.toString());
            result.append(String.format("\n📊 **BMI指数**：%.1f\n", bmi));
            result.append(String.format("📌 **状态**：%s\n\n", bmiStatus));
            result.append("💡 身体数据已保存，可用于制定饮食计划");

            log.info("✅ [Tool] 更新身体数据成功，BMI: {}", bmi);
            return result.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 更新身体数据失败", e);
            return "❌ 更新失败：" + e.getMessage();
        }
    }

    /**
     * 分析用户资料完整度
     *
     * @param userId 用户ID
     * @return 完整度分析
     */
    @Tool("""
        分析用户资料的完整程度

        **检查项：**
        - 基本信息（昵称、性别、手机号）
        - 身体数据（身高、体重）
        - 健康目标
        - 饮食偏好

        **何时使用：**
        - 引导用户完善资料
        - 提升个性化体验

        **无需参数**，userId自动从上下文获取

        **返回：** 完整度评分和待完善项
        """)
    public String analyzeProfileCompleteness(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 分析用户资料完整度，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            int totalItems = 8;
            int completedItems = 0;
            StringBuilder suggestions = new StringBuilder();

            // 检查基本信息
            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                completedItems++;
            } else {
                suggestions.append("• 设置昵称\n");
            }

            if (user.getGender() != null && !user.getGender().isEmpty()) {
                completedItems++;
            } else {
                suggestions.append("• 设置性别\n");
            }

            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                completedItems++;
            } else {
                suggestions.append("• 绑定手机号\n");
            }

            // 检查身体数据
            if (user.getHeight() != null && user.getHeight() > 0) {
                completedItems++;
            } else {
                suggestions.append("• 填写身高\n");
            }

            if (user.getWeight() != null && user.getWeight() > 0) {
                completedItems++;
            } else {
                suggestions.append("• 填写体重\n");
            }

            // 检查健康目标
            if (user.getDietGoal() != null && !user.getDietGoal().isEmpty()) {
                completedItems++;
            } else {
                suggestions.append("• 设置健康目标\n");
            }

            // 检查偏好
            if (user.getPreferTags() != null && !user.getPreferTags().isEmpty()) {
                completedItems++;
            } else {
                suggestions.append("• 设置饮食偏好\n");
            }

            // 检查过敏
            if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
                completedItems++;
            } else {
                suggestions.append("• 填写过敏信息（如无则可忽略）\n");
            }

            double completeness = (double) completedItems / totalItems * 100;
            String level = completeness >= 80 ? "完善" :
                          completeness >= 60 ? "良好" :
                          completeness >= 40 ? "一般" : "需完善";

            StringBuilder result = new StringBuilder();
            result.append("📊 用户资料完整度分析\n\n");
            result.append(String.format("🎯 完整度：%.0f%% (%d/%d)\n", completeness, completedItems, totalItems));
            result.append(String.format("📌 评级：%s\n\n", level));

            if (completedItems < totalItems) {
                result.append("💡 **建议完善以下信息：**\n");
                result.append(suggestions.toString());
            } else {
                result.append("🎉 恭喜！您的资料已完整填写\n");
                result.append("💡 完善的资料可以帮助我们提供更精准的推荐");
            }

            log.info("✅ [Tool] 分析完整度完成，完整度: {}%", completeness);
            return result.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 分析完整度失败", e);
            return "❌ 分析失败：" + e.getMessage();
        }
    }

    /**
     * 获取资料完善建议
     *
     * @param userId 用户ID
     * @return 完善建议
     */
    @Tool("""
        根据用户当前资料提供完善建议

        **建议内容：**
        - 优先完善的字段
        - 完善后的好处
        - 如何完善

        **何时使用：**
        - 新用户引导
        - 提升资料完整度

        **无需参数**，userId自动从上下文获取

        **返回：** 完善建议
        """)
    public String getProfileImprovementSuggestions(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 获取资料完善建议，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            StringBuilder suggestions = new StringBuilder();
            suggestions.append("💡 资料完善建议\n\n");

            // 优先级1：身体数据
            if (user.getHeight() == null || user.getWeight() == null) {
                suggestions.append("🔥 **高优先级：完善身体数据**\n");
                suggestions.append("  • 填写身高和体重\n");
                suggestions.append("  • 好处：系统可以计算BMI和热量目标\n");
                suggestions.append("  • 如何：在个人中心点击\"身体数据\"\n\n");
            }

            // 优先级2：健康目标
            if (user.getDietGoal() == null || user.getDietGoal().isEmpty()) {
                suggestions.append("⭐ **中优先级：设置健康目标**\n");
                suggestions.append("  • 选择目标：减肥/增肌/保持/增重\n");
                suggestions.append("  • 好处：系统提供个性化的饮食推荐\n");
                suggestions.append("  • 如何：在\"健康\"页面设置目标\n\n");
            }

            // 优先级3：饮食偏好
            if (user.getPreferTags() == null || user.getPreferTags().isEmpty()) {
                suggestions.append("📝 **建议：设置饮食偏好**\n");
                suggestions.append("  • 选择喜欢的口味和菜系\n");
                suggestions.append("  • 好处：推荐更符合您口味的菜品\n");
                suggestions.append("  • 如何：在\"偏好\"页面选择标签\n\n");
            }

            // 优先级4：基本信息
            if (user.getNickname() == null || user.getGender() == null) {
                suggestions.append("👤 **完善基本信息**\n");
                suggestions.append("  • 设置昵称和性别\n");
                suggestions.append("  • 好处：让助手更好地称呼您\n\n");
            }

            if (suggestions.length() == 25) { // 只有标题
                suggestions.append("🎉 您的资料已经很完整了！\n");
                suggestions.append("💡 建议定期更新体重和健康目标，以获得更精准的建议");
            }

            log.info("✅ [Tool] 生成完善建议成功");
            return suggestions.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 生成完善建议失败", e);
            return "❌ 生成失败：" + e.getMessage();
        }
    }

    /**
     * 计算BMI
     */
    private double calculateBMI(User user) {
        if (user.getHeight() == null || user.getWeight() == null) {
            return 0;
        }
        double heightM = user.getHeight() / 100.0;
        return user.getWeight() / (heightM * heightM);
    }

    /**
     * 获取BMI状态
     */
    private String getBMIStatus(double bmi) {
        if (bmi == 0) return "未知";
        if (bmi < 18.5) return "偏瘦";
        if (bmi < 24) return "正常";
        if (bmi < 28) return "偏胖";
        return "肥胖";
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

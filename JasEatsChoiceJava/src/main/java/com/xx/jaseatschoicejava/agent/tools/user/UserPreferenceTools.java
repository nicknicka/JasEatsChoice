package com.xx.jaseatschoicejava.agent.tools.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xx.jaseatschoicejava.agent.dto.UserDietPreference;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.scope.AgenticScope;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户偏好工具类
 *
 * 为Agent提供用户偏好的查询和管理功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class UserPreferenceTools {

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取用户饮食偏好
     *
     * @param userId 用户ID
     * @return 饮食偏好信息
     */
    @Tool("""
        获取用户的饮食偏好，包括：
        - 口味偏好（辣度、甜度等）
        - 菜系偏好（川菜、粤菜等）
        - 素食/荤食
        - 价格区间
        - 营养需求（低卡、低脂等）
        - 过敏食材列表

        **何时使用：**
        - 个性化推荐
        - 菜品筛选
        - 制定饮食计划

        **无需参数**，userId自动从上下文获取

        **返回：** 饮食偏好信息
        """)
    public UserDietPreference getDietPreference(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return UserDietPreference.builder()
                    .userId(null)
                    .exists(false)
                    .build();
        }
        log.info("🔍 [Tool] 查询用户饮食偏好，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return UserDietPreference.builder()
                        .userId(userId)
                        .exists(false)
                        .build();
            }

            // 解析preferTags JSON
            JsonNode preferTags = user.getPreferTags();
            int spicyLevel = 3;
            int sweetLevel = 3;
            String cuisinePreference = "无偏好";
            String dietType = "混合";
            String priceRange = "中";
            String nutritionNeeds = "均衡";

            if (preferTags != null && preferTags.isObject()) {
                if (preferTags.has("spicyLevel")) {
                    spicyLevel = preferTags.get("spicyLevel").asInt();
                }
                if (preferTags.has("sweetLevel")) {
                    sweetLevel = preferTags.get("sweetLevel").asInt();
                }
                if (preferTags.has("cuisine")) {
                    cuisinePreference = preferTags.get("cuisine").asText();
                }
                if (preferTags.has("dietType")) {
                    dietType = preferTags.get("dietType").asText();
                }
                if (preferTags.has("priceRange")) {
                    priceRange = preferTags.get("priceRange").asText();
                }
                if (preferTags.has("nutritionNeeds")) {
                    nutritionNeeds = preferTags.get("nutritionNeeds").asText();
                }
            }

            UserDietPreference preference = UserDietPreference.builder()
                    .userId(user.getUserId())
                    .spicyLevel(spicyLevel)
                    .sweetLevel(sweetLevel)
                    .cuisinePreference(cuisinePreference)
                    .dietType(dietType)
                    .priceRange(priceRange)
                    .nutritionNeeds(nutritionNeeds)
                    .allergies(user.getAllergies())
                    .preferTags(user.getPreferTags())
                    .exists(true)
                    .build();

            log.info("✅ [Tool] 查询用户偏好成功: {}", preference.getCuisinePreference());
            return preference;

        } catch (RuntimeException e) {
            log.error("❌ [Tool] 查询用户饮食偏好失败，userId: {}", userId, e);
            return UserDietPreference.builder()
                    .userId(userId)
                    .exists(false)
                    .build();
        }
    }

    /**
     * 更新用户饮食偏好
     *
     * @param userId 用户ID
     * @param preferenceJson 偏好信息（JSON格式）
     * @return 更新结果
     */
    @Tool("""
        更新用户的饮食偏好

        **支持的字段：**
        - spicyLevel: 辣度（1-5）
        - sweetLevel: 甜度（1-5）
        - cuisine: 菜系（川菜、粤菜等）
        - dietType: 饮食类型（素食/荤食/混合）
        - priceRange: 价格区间（低/中/高）
        - nutritionNeeds: 营养需求（低卡/低脂/高蛋白/均衡）

        **何时使用：**
        - 用户明确表示喜欢/不喜欢某类食物
        - 用户修改偏好设置

        **参数：**
        - **无需参数**，userId自动从上下文获取
        - preferenceJson - 偏好信息（JSON格式）

        **返回：** 更新结果
        """)
    public String updateDietPreference(
        AgenticScope scope,
        @P("偏好信息（JSON格式）") String preferenceJson
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }

        if (!isPreferenceWriteAllowed(scope)) {
            log.warn("⛔ [Tool] 拒绝未授权的偏好写入，userId: {}, preference: {}", userId, preferenceJson);
            return "❌ 当前请求未获得修改偏好的授权，仅允许查询用户饮食偏好";
        }

        log.info("🔍 [Tool] 更新用户饮食偏好，userId: {}, preference: {}", userId, preferenceJson);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return "❌ 用户不存在";
            }

            // 解析新的偏好JSON
            JsonNode newPreference = objectMapper.readTree(preferenceJson);

            // 合并现有偏好
            ObjectNode mergedPreference;
            if (user.getPreferTags() != null && user.getPreferTags().isObject()) {
                mergedPreference = objectMapper.createObjectNode();
                // 复制现有偏好
                user.getPreferTags().fields().forEachRemaining(entry ->
                    mergedPreference.set(entry.getKey(), entry.getValue())
                );
            } else {
                mergedPreference = objectMapper.createObjectNode();
            }

            // 更新新偏好
            newPreference.fields().forEachRemaining(entry ->
                mergedPreference.set(entry.getKey(), entry.getValue())
            );

            // 保存到数据库
            user.setPreferTags(mergedPreference);
            userService.updateById(user);

            log.info("✅ [Tool] 更新用户偏好成功");
            return "✅ 偏好更新成功：" + mergedPreference.toString();

        } catch (JsonProcessingException e) {
            log.error("❌ [Tool] 更新用户饮食偏好失败，userId: {}", userId, e);
            return "❌ 更新失败：" + e.getMessage();
        } catch (RuntimeException e) {
            log.error("❌ [Tool] 更新用户饮食偏好失败，userId: {}", userId, e);
            return "❌ 更新失败：" + e.getMessage();
        }
    }

    /**
     * 添加用户忌口/过敏食物
     *
     * @param userId 用户ID
     * @param foodItem 忌口食物
     * @return 添加结果
     */
    @Tool("""
        添加用户的忌口或过敏食物

        **何时使用：**
        - 用户表示对某食物过敏
        - 用户不想吃某类食物
        - 用户有饮食禁忌

        **参数：**
        - **无需参数**，userId自动从上下文获取
        - foodItem - 忌口食物名称

        **返回：** 添加结果
        """)
    public String addAllergy(
        AgenticScope scope,
        @P("忌口食物") String foodItem
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }

        if (!isPreferenceWriteAllowed(scope)) {
            log.warn("⛔ [Tool] 拒绝未授权的忌口写入，userId: {}, food: {}", userId, foodItem);
            return "❌ 当前请求未获得修改忌口的授权，仅允许查询用户饮食偏好";
        }

        log.info("🔍 [Tool] 添加用户忌口，userId: {}, food: {}", userId, foodItem);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return "❌ 用户不存在";
            }

            // 获取现有过敏列表
            List<String> allergyList = new ArrayList<>();
            if (user.getAllergies() != null && user.getAllergies().isArray()) {
                for (JsonNode node : user.getAllergies()) {
                    allergyList.add(node.asText());
                }
            }

            // 检查是否已存在
            if (allergyList.contains(foodItem)) {
                log.info("⚠️ [Tool] 忌口已存在: {}", foodItem);
                return "⚠️ 该食物已在忌口列表中";
            }

            // 添加新的忌口
            allergyList.add(foodItem);

            // 转换为JSON并保存
            ArrayNode allergiesArray = objectMapper.createArrayNode();
            allergyList.forEach(allergiesArray::add);

            user.setAllergies(allergiesArray);
            userService.updateById(user);

            log.info("✅ [Tool] 添加忌口成功: {}", foodItem);
            return "✅ 忌口添加成功：" + foodItem;

        } catch (RuntimeException e) {
            log.error("❌ [Tool] 添加用户忌口失败，userId: {}", userId, e);
            return "❌ 添加失败：" + e.getMessage();
        }
    }

    /**
     * 移除用户忌口
     *
     * @param userId 用户ID
     * @param foodItem 忌口食物
     * @return 移除结果
     */
    @Tool("""
        移除用户的忌口或过敏食物

        **何时使用：**
        - 用户表示不再对某食物过敏
        - 用户想要移除饮食禁忌

        **参数：**
        - **无需参数**，userId自动从上下文获取
        - foodItem - 忌口食物名称

        **返回：** 移除结果
        """)
    public String removeAllergy(
        AgenticScope scope,
        @P("忌口食物") String foodItem
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 移除用户忌口，userId: {}, food: {}", userId, foodItem);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return "❌ 用户不存在";
            }

            // 获取现有过敏列表
            List<String> allergyList = new ArrayList<>();
            if (user.getAllergies() != null && user.getAllergies().isArray()) {
                for (JsonNode node : user.getAllergies()) {
                    allergyList.add(node.asText());
                }
            }

            // 检查是否存在
            if (!allergyList.contains(foodItem)) {
                log.info("⚠️ [Tool] 忌口不存在: {}", foodItem);
                return "⚠️ 该食物不在忌口列表中";
            }

            // 移除忌口
            allergyList.remove(foodItem);

            // 转换为JSON并保存
            ArrayNode allergiesArray = objectMapper.createArrayNode();
            allergyList.forEach(allergiesArray::add);

            user.setAllergies(allergiesArray);
            userService.updateById(user);

            log.info("✅ [Tool] 移除忌口成功: {}", foodItem);
            return "✅ 忌口移除成功：" + foodItem;

        } catch (RuntimeException e) {
            log.error("❌ [Tool] 移除用户忌口失败，userId: {}", userId, e);
            return "❌ 移除失败：" + e.getMessage();
        }
    }

    /**
     * 获取用户忌口列表
     *
     * @param userId 用户ID
     * @return 忌口列表
     */
    @Tool("""
        获取用户的忌口或过敏食物列表

        **何时使用：**
        - 推荐菜品时需要过滤
        - 检查食物是否安全

        **无需参数**，userId自动从上下文获取

        **返回：** 忌口食物列表
        """)
    public List<String> getAllergyList(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return List.of();
        }
        log.info("🔍 [Tool] 查询用户忌口列表，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null || user.getAllergies() == null) {
                return List.of();
            }

            List<String> allergyList = new ArrayList<>();
            if (user.getAllergies().isArray()) {
                for (JsonNode node : user.getAllergies()) {
                    allergyList.add(node.asText());
                }
            }

            log.info("✅ [Tool] 查询忌口列表成功，数量: {}", allergyList.size());
            return allergyList;

        } catch (RuntimeException e) {
            log.error("❌ [Tool] 查询用户忌口列表失败，userId: {}", userId, e);
            return List.of();
        }
    }

    private boolean isPreferenceWriteAllowed(AgenticScope scope) {
        if (scope == null) {
            return false;
        }

        Object allowed = scope.readState("preferenceWriteAllowed");
        if (allowed == null) {
            return false;
        }

        if (allowed instanceof Boolean booleanAllowed) {
            return booleanAllowed;
        }

        return Boolean.parseBoolean(String.valueOf(allowed));
    }
}

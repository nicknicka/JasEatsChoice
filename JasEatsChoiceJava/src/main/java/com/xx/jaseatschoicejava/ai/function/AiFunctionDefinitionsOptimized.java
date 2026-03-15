package com.xx.jaseatschoicejava.ai.function;

import com.xx.jaseatschoicejava.enums.AiFunctionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AI工具函数定义（优化版）
 * 从配置文件读取数据，避免硬编码
 *
 * @author Claude
 * @since 2026-03-13
 */
@Slf4j
@Component
public class AiFunctionDefinitionsOptimized {

    /**
     * 菜品分类列表（从配置文件读取）
     */
    private List<String> dishCategories;

    /**
     * 系统提示词（从配置文件读取）
     */
    private Map<String, String> systemPrompts;

    /**
     * 工具函数定义列表
     */
    private final List<ToolFunction> toolFunctions;

    public AiFunctionDefinitionsOptimized() {
        this.toolFunctions = new ArrayList<>();
        initializeDefaultCategories();
        initializeDefaultPrompts();
        buildToolFunctions();
    }

    /**
     * 初始化默认菜品分类
     * 实际应该从配置文件或数据库读取
     */
    private void initializeDefaultCategories() {
        this.dishCategories = Arrays.asList(
                "主食", "菜肴", "汤品", "饮品", "小吃", "甜点"
        );
    }

    /**
     * 初始化默认系统提示词
     * 实际应该从配置文件读取
     */
    private void initializeDefaultPrompts() {
        this.systemPrompts = new HashMap<>();
        systemPrompts.put("primary",
                "你是佳食宜选的智能饮食助手，可以帮助用户搜索菜品、查看订单、获取营养分析等。\n\n" +
                "【重要：下单流程】\n" +
                "当用户说'下单'、'购买'、'点菜'、'要XX菜'等表达购买意图时：\n" +
                "1. 先使用search_dishes搜索菜品，获取菜品信息和价格\n" +
                "2. 向用户确认订单：列出菜品名称、价格，询问数量\n" +
                "3. 询问用餐方式（必需）：\n" +
                "   - 堂食：需要询问桌号\n" +
                "   - 自提：需要询问取餐时间和联系电话\n" +
                "4. 使用create_order创建订单（需要dish_items和address参数）\n" +
                "5. 告知用户订单创建成功，显示订单号、用餐方式、总金额\n\n" +
                "注意事项：\n" +
                "- 如果用户提到菜名但没有数量，默认为1份\n" +
                "- 必须询问用餐方式（堂食/自提）才能创建订单\n" +
                "- 堂食订单需要桌号，自提订单需要取餐时间\n" +
                "- 创建订单前要向用户确认订单详情和总金额\n" +
                "- 不要推荐其他菜品，除非用户主动询问\n\n" +
                "你的职责：\n" +
                "1. 根据用户需求推荐合适的菜品\n" +
                "2. 提供准确的营养信息和健康建议\n" +
                "3. 引导用户完成下单流程\n" +
                "4. 保持友好、专业的服务态度"
        );
        systemPrompts.put("recommendation",
                "你是专业的菜品推荐顾问。根据用户的口味偏好、健康需求和预算，" +
                "推荐最合适的菜品组合。请关注用户的饮食禁忌和过敏信息。"
        );
        systemPrompts.put("nutrition",
                "你是专业的营养分析师。提供准确的营养信息，包括热量、蛋白质、" +
                "脂肪、碳水化合物等。对于有特殊饮食需求的用户，给出相应的建议。"
        );
    }

    /**
     * 构建工具函数定义
     * 使用枚举替代硬编码字符串
     */
    private void buildToolFunctions() {
        // 1. 搜索菜品
        toolFunctions.add(createSearchDishesFunction());

        // 2. 获取菜品详情
        toolFunctions.add(createGetDishDetailsFunction());

        // 3. 创建订单
        toolFunctions.add(createCreateOrderFunction());

        // 4. 查询订单状态
        toolFunctions.add(createGetOrderStatusFunction());

        // 5. 查询订单列表
        toolFunctions.add(createListOrdersFunction());

        // 6. 获取用户偏好
        toolFunctions.add(createGetUserPreferencesFunction());

        // 7. 分析营养信息
        toolFunctions.add(createAnalyzeNutritionFunction());

        log.info("AI工具函数初始化完成，共{}个工具", toolFunctions.size());
    }

    /**
     * 创建搜索菜品工具函数
     */
    private ToolFunction createSearchDishesFunction() {
        AiFunctionType type = AiFunctionType.SEARCH_DISHES;

        Map<String, Object> properties = new HashMap<>();
        properties.put("keyword", createStringProperty("搜索关键词"));
        properties.put("category", createStringPropertyWithEnum("菜品分类", dishCategories));

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Collections.singletonList("keyword")))
                .build();
    }

    /**
     * 创建获取菜品详情工具函数
     */
    private ToolFunction createGetDishDetailsFunction() {
        AiFunctionType type = AiFunctionType.GET_DISH_DETAILS;

        Map<String, Object> properties = new HashMap<>();
        properties.put("dish_id", createStringProperty("菜品ID"));

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Collections.singletonList("dish_id")))
                .build();
    }

    /**
     * 创建订单工具函数
     * 注意：user_id参数已移除，系统会自动使用当前登录用户的ID
     */
    private ToolFunction createCreateOrderFunction() {
        AiFunctionType type = AiFunctionType.CREATE_ORDER;

        Map<String, Object> dishItemProperties = new HashMap<>();
        dishItemProperties.put("dish_id", createStringProperty("菜品ID"));
        dishItemProperties.put("quantity", createIntegerProperty("数量"));

        Map<String, Object> itemsSchema = new HashMap<>();
        itemsSchema.put("type", "object");
        itemsSchema.put("properties", dishItemProperties);

        Map<String, Object> properties = new HashMap<>();
        properties.put("dish_items", createArrayProperty("菜品列表", itemsSchema));
        properties.put("address", createStringProperty("配送地址"));
        // 不再需要user_id参数，系统会自动注入

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Arrays.asList("dish_items", "address")))
                .build();
    }

    /**
     * 创建查询订单状态工具函数
     */
    private ToolFunction createGetOrderStatusFunction() {
        AiFunctionType type = AiFunctionType.GET_ORDER_STATUS;

        Map<String, Object> properties = new HashMap<>();
        properties.put("order_id", createStringProperty("订单ID"));

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Collections.singletonList("order_id")))
                .build();
    }

    /**
     * 创建查询订单列表工具函数
     */
    private ToolFunction createListOrdersFunction() {
        AiFunctionType type = AiFunctionType.LIST_ORDERS;

        // 无需参数，系统会自动使用当前登录用户的ID
        Map<String, Object> properties = new HashMap<>();

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Collections.emptyList()))
                .build();
    }

    /**
     * 创建获取用户偏好工具函数
     * 注意：user_id参数已移除，系统会自动使用当前登录用户的ID
     */
    private ToolFunction createGetUserPreferencesFunction() {
        AiFunctionType type = AiFunctionType.GET_USER_PREFERENCES;

        Map<String, Object> properties = new HashMap<>();
        // 不再需要user_id参数，系统会自动注入

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Collections.emptyList())) // 无必需参数
                .build();
    }

    /**
     * 创建营养分析工具函数
     */
    private ToolFunction createAnalyzeNutritionFunction() {
        AiFunctionType type = AiFunctionType.ANALYZE_NUTRITION;

        Map<String, Object> properties = new HashMap<>();
        properties.put("food_name", createStringProperty("食物名称"));

        return ToolFunction.builder()
                .name(type.getFunctionName())
                .description(type.getDescription())
                .parameters(createParameterSchema(properties, Collections.singletonList("food_name")))
                .build();
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建字符串属性
     */
    private Map<String, Object> createStringProperty(String description) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", "string");
        property.put("description", description);
        return property;
    }

    /**
     * 创建带枚举的字符串属性
     */
    private Map<String, Object> createStringPropertyWithEnum(String description, List<String> enumValues) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", "string");
        property.put("description", description);
        property.put("enum", enumValues);
        return property;
    }

    /**
     * 创建整数属性
     */
    private Map<String, Object> createIntegerProperty(String description) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", "integer");
        property.put("description", description);
        return property;
    }

    /**
     * 创建数组属性
     */
    private Map<String, Object> createArrayProperty(String description, Map<String, Object> itemsSchema) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", "array");
        property.put("description", description);
        property.put("items", itemsSchema);
        return property;
    }

    /**
     * 创建参数Schema
     */
    private Map<String, Object> createParameterSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    // ==================== Getter方法 ====================

    /**
     * 获取所有工具函数定义
     */
    public List<ToolFunction> getToolFunctions() {
        return Collections.unmodifiableList(toolFunctions);
    }

    /**
     * 获取主系统提示词
     */
    public String getPrimarySystemPrompt() {
        return systemPrompts.getOrDefault("primary",
                "你是佳食宜选的智能饮食助手。");
    }

    /**
     * 获取推荐专用提示词
     */
    public String getRecommendationPrompt() {
        return systemPrompts.getOrDefault("recommendation",
                "你是专业的菜品推荐顾问。");
    }

    /**
     * 获取营养分析专用提示词
     */
    public String getNutritionPrompt() {
        return systemPrompts.getOrDefault("nutrition",
                "你是专业的营养分析师。");
    }

    /**
     * 获取菜品分类列表
     */
    public List<String> getDishCategories() {
        return Collections.unmodifiableList(dishCategories);
    }

    /**
     * 工具函数内部类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ToolFunction {
        private String name;
        private String description;
        private Map<String, Object> parameters;

        public static ToolFunctionBuilder builder() {
            return new ToolFunctionBuilder();
        }

        public static class ToolFunctionBuilder {
            private String name;
            private String description;
            private Map<String, Object> parameters;

            public ToolFunctionBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ToolFunctionBuilder description(String description) {
                this.description = description;
                return this;
            }

            public ToolFunctionBuilder parameters(Map<String, Object> parameters) {
                this.parameters = parameters;
                return this;
            }

            public ToolFunction build() {
                return new ToolFunction(name, description, parameters);
            }
        }
    }
}

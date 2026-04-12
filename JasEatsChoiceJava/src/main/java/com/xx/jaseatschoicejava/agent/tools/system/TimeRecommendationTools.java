package com.xx.jaseatschoicejava.agent.tools.system;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.DishFeature;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.service.DishService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 时段推荐工具类
 *
 * 为Agent提供时段相关的推荐功能，优先查询真实菜品数据，无数据时降级到静态模板
 *
 * @author Claude
 * @since 2026-03-27
 */
@Slf4j
@Service
public class TimeRecommendationTools {

    private final DishService dishService;
    private final DishFeatureMapper dishFeatureMapper;

    public TimeRecommendationTools(DishService dishService, DishFeatureMapper dishFeatureMapper) {
        this.dishService = dishService;
        this.dishFeatureMapper = dishFeatureMapper;
    }

    /**
     * 根据时段推荐菜品
     */
    @Tool("""
        根据当前时段推荐合适的菜品

        **时段推荐策略：**
        - 早晨（5:00-8:00）：营养丰富的早餐
        - 上午（8:00-11:00）：可预订午餐
        - 中午（11:00-13:00）：均衡营养的午餐
        - 下午（13:00-17:00）：下午茶或预订晚餐
        - 晚上（17:00-20:00）：清淡易消化的晚餐
        - 深夜（20:00-5:00）：尽量避免或选择易消化食物

        **参数：**
        - timeSlot - 时段（早晨/上午/中午/下午/晚上/深夜）
        - userPreference - 用户偏好（可选）

        **返回：** 时段菜品推荐
        """)
    public String recommendDishesByTimeSlot(
        @P("时段（早晨/上午/中午/下午/晚上/深夜）") String timeSlot,
        @P("用户偏好（可选）") String userPreference
    ) {
        log.info("🔍 [Tool] 时段推荐菜品，timeSlot: {}, preference: {}", timeSlot, userPreference);

        try {
            String result = buildRealDishRecommendation(timeSlot);
            if (result != null) {
                log.info("✅ [Tool] 时段推荐菜品成功（真实数据）");
                return result;
            }

            // 降级到静态模板
            log.info("⏭️ [Tool] 无真实菜品数据，降级到静态模板");
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🕐 **%s菜品推荐**\n\n", timeSlot));
            sb.append(buildStaticRecommendation(timeSlot));
            log.info("✅ [Tool] 时段推荐菜品成功（静态模板）");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 时段推荐菜品失败", e);
            return "❌ 推荐失败：" + e.getMessage();
        }
    }

    /**
     * 一次性返回早餐/午餐/晚餐推荐，减少Agent多次工具调用。
     */
    @Tool("""
        一次性生成今日三餐（早餐/午餐/晚餐）推荐。

        **适用场景：**
        - 用户要求"安排今天三餐"
        - 用户希望获取一整天饮食搭配
        - 需要减少多次工具调用、提升响应速度

        **参数：**
        - userPreference - 用户偏好（可选）

        **返回：** 按早餐/午餐/晚餐分段的建议
        """)
    public String recommendDailyMealsByTimeSlots(
        @P("用户偏好（可选）") String userPreference
    ) {
        log.info("🔍 [Tool] 批量时段推荐（三餐），preference: {}", userPreference);

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🍽️ **今日三餐搭配建议**\n\n");

            // 尝试查询真实菜品
            String breakfast = buildRealDishRecommendation("早晨");
            String lunch = buildRealDishRecommendation("中午");
            String dinner = buildRealDishRecommendation("晚上");

            sb.append("【早餐】\n");
            sb.append(breakfast != null ? breakfast : buildStaticRecommendation("早晨"));
            sb.append("\n\n");

            sb.append("【午餐】\n");
            sb.append(lunch != null ? lunch : buildStaticRecommendation("中午"));
            sb.append("\n\n");

            sb.append("【晚餐】\n");
            sb.append(dinner != null ? dinner : buildStaticRecommendation("晚上"));

            if (userPreference != null && !userPreference.isBlank()) {
                sb.append("\n\n🎯 **偏好补充**\n");
                sb.append("- 已参考您的偏好：").append(userPreference).append("\n");
                sb.append("- 如需更细化，可继续说明忌口、预算或口味偏好");
            }

            log.info("✅ [Tool] 批量时段推荐（三餐）成功");
            return sb.toString();
        } catch (Exception e) {
            log.error("❌ [Tool] 批量时段推荐（三餐）失败", e);
            return "❌ 推荐失败：" + e.getMessage();
        }
    }

    /**
     * 查询当前营业的商家
     */
    @Tool("""
        查询当前时间营业中的商家

        **参数：** currentTime - 当前时间（可选）

        **返回：** 营业状态说明
        """)
    public String getOpenMerchants(
        @P("当前时间HH:mm，可选") String currentTime
    ) {
        log.info("🔍 [Tool] 查询营业中的商家，currentTime: {}", currentTime);

        try {
            LocalTime now = LocalTime.now();
            if (currentTime != null && !currentTime.isEmpty()) {
                now = LocalTime.parse(currentTime);
            }

            String period = getTimePeriod(now);

            StringBuilder sb = new StringBuilder();
            sb.append("🏪 商家营业状态\n\n");
            sb.append(String.format("🕐 当前时间：%s\n", now.format(DateTimeFormatter.ofPattern("HH:mm"))));
            sb.append(String.format("📌 时段：%s\n\n", period));

            switch (period) {
                case "中午", "晚上" -> {
                    sb.append("✅ **大部分商家正在营业**\n\n");
                    sb.append("⚠️ **高峰提示**\n");
                    sb.append("  • 当前为用餐高峰期\n");
                    sb.append("  • 建议提前下单避免等待\n");
                    sb.append("  • 配送时间可能延长");
                }
                case "深夜" -> {
                    sb.append("⚠️ **部分商家已打烊**\n\n");
                    sb.append("💡 **建议**\n");
                    sb.append("  • 选择24小时营业的商家\n");
                    sb.append("  • 提前确认营业时间\n");
                    sb.append("  • 建议选择快餐类");
                }
                default -> {
                    sb.append("✅ **大部分商家正常营业**\n\n");
                    sb.append("💡 **建议**\n");
                    sb.append("  • 当前时段订单较少\n");
                    sb.append("  • 配送速度较快");
                }
            }

            log.info("✅ [Tool] 查询营业中的商家成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询营业中的商家失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 计算最佳订餐时间
     */
    @Tool("""
        计算最佳订餐时间（避开高峰）

        **参数：**
        - merchantId - 商家ID（可选）
        - targetTime - 目标用餐时间（可选）

        **返回：** 最佳订餐时间建议
        """)
    public String calculateBestOrderTime(
        @P("商家ID（可选）") String merchantId,
        @P("目标用餐时间，如12:00（可选）") String targetTime
    ) {
        log.info("🔍 [Tool] 计算最佳订餐时间，merchant: {}, targetTime: {}", merchantId, targetTime);

        try {
            LocalTime now = LocalTime.now();
            String period = getTimePeriod(now);

            StringBuilder sb = new StringBuilder();
            sb.append("⏰ 最佳订餐时间建议\n\n");
            sb.append(String.format("🕐 当前时间：%s\n", now.format(DateTimeFormatter.ofPattern("HH:mm"))));
            sb.append(String.format("📌 当前时段：%s\n\n", period));

            switch (period) {
                case "早晨" -> {
                    sb.append("💡 **早餐时段**\n");
                    sb.append("  • 建议：现点现吃\n");
                    sb.append("  • 推荐时间：7:00-8:00");
                }
                case "上午" -> {
                    sb.append("💡 **午餐预订**\n");
                    sb.append("  • 最佳下单时间：11:00前\n");
                    sb.append("  • 可避开11:30-12:30高峰\n");
                    sb.append("  • 提前10-15分钟下单");
                }
                case "中午" -> {
                    sb.append("⚠️ **午餐高峰期**\n");
                    sb.append("  • 高峰时间：11:30-13:00\n");
                    sb.append("  • 配送时间：30-40分钟");
                }
                case "下午" -> {
                    sb.append("💡 **晚餐预订**\n");
                    sb.append("  • 最佳下单时间：17:00前\n");
                    sb.append("  • 可避开17:30-19:00高峰\n");
                    sb.append("  • 提前15-20分钟下单");
                }
                case "晚上" -> {
                    sb.append("⚠️ **晚餐高峰期**\n");
                    sb.append("  • 高峰时间：17:30-19:00\n");
                    sb.append("  • 配送时间：35-45分钟");
                }
                case "深夜" -> {
                    sb.append("🌙 **深夜时段**\n");
                    sb.append("  • 确认商家营业状态\n");
                    sb.append("  • 配送时间：15-20分钟");
                }
            }

            log.info("✅ [Tool] 计算最佳订餐时间成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 计算最佳订餐时间失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    // ==================== 真实菜品查询 ====================

    /**
     * 根据时段查询真实菜品数据
     *
     * @param timeSlot 时段（早晨/上午/中午/下午/晚上/深夜）
     * @return 真实菜品推荐文本，无数据时返回null
     */
    private String buildRealDishRecommendation(String timeSlot) {
        String timePeriodTag = mapTimeSlotToPeriodTag(timeSlot);
        if (timePeriodTag == null) {
            return null;
        }

        try {
            // 1. 通过 dish_features 的 time_period_tags 查询匹配的菜品特征
            List<DishFeature> features = dishFeatureMapper.getByTimePeriod(timePeriodTag);
            if (features == null || features.isEmpty()) {
                log.info("📭 [Tool] dish_features 中无时段={} 的数据", timePeriodTag);
                return null;
            }

            // 2. 提取 dishId 列表，查询真实菜品
            List<String> dishIds = features.stream()
                .map(DishFeature::getDishId)
                .toList();

            List<Dish> dishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .in(Dish::getId, dishIds)
                    .eq(Dish::getIsOnline, true)
                    .orderByDesc(Dish::getScore)
                    .last("LIMIT 6")
            );

            if (dishes.isEmpty()) {
                log.info("📭 [Tool] t_dish 中无上架菜品匹配时段={}", timePeriodTag);
                return null;
            }

            // 3. 格式化输出
            StringBuilder sb = new StringBuilder();
            String emoji = getTimeSlotEmoji(timeSlot);
            sb.append(String.format("%s **%s真实菜品推荐**\n\n", emoji, timeSlot));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   💰 %.2f元 | 🔥 %d千卡 | ⭐ %.1f分\n" +
                    "   📝 %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getCalorie() != null ? dish.getCalorie() : 0,
                    dish.getAvgRating() != null ? dish.getAvgRating().doubleValue() : 0,
                    dish.getDescription() != null && !dish.getDescription().isEmpty()
                        ? truncate(dish.getDescription(), 50)
                        : "暂无描述"
                ));
            }

            sb.append("📊 数据来源：真实菜品库（共匹配").append(dishes.size()).append("道菜品）\n");
            return sb.toString();

        } catch (Exception e) {
            log.warn("⚠️ [Tool] 查询真实菜品数据失败，将降级到静态模板: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将内部时段名称映射到 dish_features.time_period_tags 的值
     */
    private String mapTimeSlotToPeriodTag(String timeSlot) {
        return switch (timeSlot) {
            case "早晨" -> "早餐";
            case "上午" -> "早餐";
            case "中午" -> "午餐";
            case "下午" -> "午餐";
            case "晚上" -> "晚餐";
            case "深夜" -> "宵夜";
            default -> null;
        };
    }

    private String getTimeSlotEmoji(String timeSlot) {
        return switch (timeSlot) {
            case "早晨" -> "🌅";
            case "上午" -> "☀️";
            case "中午" -> "🌞";
            case "下午" -> "☕";
            case "晚上" -> "🌙";
            case "深夜" -> "🌜";
            default -> "🍽️";
        };
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }

    // ==================== 静态模板（降级兜底） ====================

    private String buildStaticRecommendation(String timeSlot) {
        StringBuilder sb = new StringBuilder();
        switch (timeSlot) {
            case "早晨" -> {
                sb.append("🌅 **早餐推荐**\n");
                sb.append("  • 蛋白质：鸡蛋、牛奶、豆浆\n");
                sb.append("  • 碳水：燕麦粥、全麦面包、红薯\n");
                sb.append("  • 维生素：水果、蔬菜沙拉\n\n");
                sb.append("💡 **推荐搭配**\n");
                sb.append("  • 鸡蛋+牛奶+全麦面包\n");
                sb.append("  • 豆浆+包子+鸡蛋\n");
                sb.append("  • 燕麦粥+鸡蛋+水果");
            }
            case "上午" -> {
                sb.append("☀️ **上午时段**\n");
                sb.append("  • 可以预订午餐，避开高峰\n");
                sb.append("  • 建议提前10-15分钟下单\n");
                sb.append("  • 工作时间可适量加餐（坚果、酸奶）");
            }
            case "中午" -> {
                sb.append("🌞 **午餐推荐**\n");
                sb.append("  • 主食：米饭、面条（适量）\n");
                sb.append("  • 蛋白质：鸡胸肉、鱼虾、豆腐\n");
                sb.append("  • 蔬菜：西兰花、菠菜、白菜\n\n");
                sb.append("💡 **营养搭配**\n");
                sb.append("  • 主食+菜品+汤，均衡搭配\n");
                sb.append("  • 七分饱，避免午后困倦");
            }
            case "下午" -> {
                sb.append("☕ **下午时段**\n");
                sb.append("  • 下午茶时间，可以适量加餐\n");
                sb.append("  • 推荐酸奶、水果、坚果\n");
                sb.append("  • 可以预订晚餐，避开高峰");
            }
            case "晚上" -> {
                sb.append("🌙 **晚餐推荐**\n");
                sb.append("  • 主食：减半（红薯、玉米）\n");
                sb.append("  • 蛋白质：鱼虾、豆腐（易消化）\n");
                sb.append("  • 蔬菜：大量蔬菜（膳食纤维）\n\n");
                sb.append("💡 **晚餐原则**\n");
                sb.append("  • 清淡为主，避免油腻\n");
                sb.append("  • 七分饱，影响睡眠");
            }
            case "深夜" -> {
                sb.append("🌜 **深夜时段**\n");
                sb.append("  • 建议不吃夜宵\n");
                sb.append("  • 如必须：热牛奶、燕麦粥、水果\n\n");
                sb.append("⚠️ **注意**\n");
                sb.append("  • 睡前3小时不进食\n");
                sb.append("  • 适量即可，不要吃太饱");
            }
            default -> sb.append("请提供正确的时段（早晨/上午/中午/下午/晚上/深夜）");
        }
        return sb.toString();
    }

    /**
     * 获取时间段名称
     */
    private String getTimePeriod(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 8) {
            return "早晨";
        } else if (hour >= 8 && hour < 11) {
            return "上午";
        } else if (hour >= 11 && hour < 13) {
            return "中午";
        } else if (hour >= 13 && hour < 17) {
            return "下午";
        } else if (hour >= 17 && hour < 20) {
            return "晚上";
        } else {
            return "深夜";
        }
    }
}

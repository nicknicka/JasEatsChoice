package com.xx.jaseatschoicejava.agent.tools.system;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 位置推荐工具类
 *
 * 为Agent提供位置相关的推荐功能
 *
 * @author Claude
 * @since 2026-03-27
 */
@Slf4j
@Service
public class LocationRecommendationTools {

    /**
     * 推荐就餐方式（堂食/自取）
     *
     * @param merchantId 商家ID
     * @param userLocation 用户位置
     * @return 就餐方式建议
     */
    @Tool("""
        推荐就餐方式（堂食或自取）

        **推荐策略：**
        - 距离<500米：推荐堂食
        - 距离500-1000米：堂食或自取均可
        - 距离>1000米：建议自取

        **何时使用：**
        - 用户询问"堂食还是自取"
        - 选择就餐方式
        - 规划就餐流程

        **参数：**
        - merchantId - 商家ID（可选）
        - userLocation - 用户位置

        **返回：** 就餐方式建议
        """)
    public String recommendDiningStyle(
        @P("商家ID（可选）") String merchantId,
        @P("用户位置，如：学生宿舍1栋") String userLocation
    ) {
        log.info("🔍 [Tool] 推荐就餐方式，merchant: {}, location: {}", merchantId, userLocation);

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🍽️ 就餐方式推荐\n\n");

            if (userLocation == null || userLocation.isEmpty()) {
                sb.append("⚠️ 请提供您的位置信息\n\n");
                sb.append("💡 **建议**\n");
                sb.append("  • 告诉我您在哪个位置\n");
                sb.append("  • 例如：学生宿舍1栋、食堂区等\n");
                sb.append("  • 我会为您推荐最合适的就餐方式");
                return sb.toString();
            }

            // 根据位置提供建议
            sb.append(String.format("📍 您的位置：%s\n\n", userLocation));

            sb.append("📊 **就餐方式对比**\n\n");

            sb.append("🏠 **堂食**\n");
            sb.append("  优点：\n");
            sb.append("  • 就餐环境好\n");
            sb.append("  • 食物热乎新鲜\n");
            sb.append("  • 可以加饭加菜\n");
            sb.append("  适合：距离近、时间充裕\n\n");

            sb.append("🚶 **自取**\n");
            sb.append("  优点：\n");
            sb.append("  • 灵活便捷\n");
            sb.append("  • 避免排队\n");
            sb.append("  • 可以带走吃\n");
            sb.append("  适合：距离远、时间紧张\n\n");

            sb.append("💡 **选择建议**\n");
            sb.append("  • 距离很近（<500米）：推荐堂食\n");
            sb.append("  • 距离适中（500-1000米）：均可\n");
            sb.append("  • 距离较远（>1000米）：推荐自取\n");
            sb.append("  • 天气不好：推荐自取\n");
            sb.append("  • 时间紧张：推荐自取");

            log.info("✅ [Tool] 推荐就餐方式成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 推荐就餐方式失败", e);
            return "❌ 推荐失败：" + e.getMessage();
        }
    }

    /**
     * 计算步行时间
     *
     * @param fromLocation 起始位置
     * @param toLocation 目标位置
     * @return 步行时间预估
     */
    @Tool("""
        计算两个位置之间的步行时间

        **计算标准：**
        - 平均速度：80米/分钟
        - 快速步行：100米/分钟
        - 悠闲步行：60米/分钟

        **何时使用：**
        - 规划就餐路线
        - 估算到达时间
        - 选择就餐方式

        **参数：**
        - fromLocation - 起始位置
        - toLocation - 目标位置

        **返回：** 步行时间预估
        """)
    public String calculateWalkingTime(
        @P("起始位置，如：学生宿舍1栋") String fromLocation,
        @P("目标位置，如：第一食堂") String toLocation
    ) {
        log.info("🔍 [Tool] 计算步行时间，from: {}, to: {}", fromLocation, toLocation);

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🚶 步行时间估算\n\n");

            sb.append(String.format("📍 起点：%s\n", fromLocation));
            sb.append(String.format("📍 终点：%s\n\n", toLocation));

            // 校园位置距离估算（简化版）
            int distance = estimateDistance(fromLocation, toLocation);

            // 计算步行时间
            int avgTime = distance / 80; // 平均速度
            int fastTime = distance / 100; // 快速步行
            int slowTime = distance / 60; // 悠闲步行

            sb.append(String.format("📏 估算距离：%d米\n\n", distance));

            sb.append("⏱️ **步行时间**\n");
            sb.append(String.format("  • 平均步速：%d分钟\n", avgTime));
            sb.append(String.format("  • 快速步行：%d分钟\n", fastTime));
            sb.append(String.format("  • 悠闲步行：%d分钟\n\n", slowTime));

            // 距离分类
            sb.append("📊 **距离评估**\n");
            if (distance < 500) {
                sb.append("  • 很近：推荐步行堂食\n");
                sb.append("  • 步行感受：轻松\n");
            } else if (distance < 1000) {
                sb.append("  • 适中：堂食或自取均可\n");
                sb.append("  • 步行感受：适中\n");
            } else {
                sb.append("  • 较远：建议自取或考虑骑车\n");
                sb.append("  • 步行感受：需要一些时间\n");
            }

            // 添加建议
            sb.append("\n💡 **建议**\n");
            if (distance < 500) {
                sb.append("  • 距离很近，推荐堂食享受热乎饭菜");
            } else if (distance < 1000) {
                sb.append("  • 可以堂食，也可以自取带走");
            } else {
                sb.append("  • 考虑自取，或等待配送");
            }

            log.info("✅ [Tool] 计算步行时间成功，距离: {}米", distance);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 计算步行时间失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    /**
     * 查询用户位置附近的商家
     *
     * @param userLocation 用户位置
     * @param radius 半径（米）
     * @return 附近商家
     */
    @Tool("""
        基于半径查询用户位置附近的商家

        **何时使用：**
        - 用户询问"附近有什么吃的"
        - 位置推荐
        - 选择就近就餐

        **参数：**
        - userLocation - 用户位置
        - radius - 搜索半径（米），默认500

        **返回：** 附近商家列表
        """)
    public String queryNearbyMerchantsByRadius(
        @P("用户位置，如：学生宿舍1栋") String userLocation,
        @P("搜索半径（米），默认500") Integer radius
    ) {
        log.info("🔍 [Tool] 基于半径查询附近商家，location: {}, radius: {}", userLocation, radius);

        try {
            if (radius == null || radius <= 0) {
                radius = 500; // 默认500米
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🏪 附近商家推荐\n\n");

            sb.append(String.format("📍 您的位置：%s\n", userLocation));
            sb.append(String.format("📏 搜索半径：%d米\n\n", radius));

            // 根据位置推荐商家（简化版）
            sb.append("💡 **推荐策略**\n");
            sb.append("  • 优先推荐距离近的商家\n");
            sb.append("  • 考虑评分和口味\n");
            sb.append("  • 确认商家营业状态\n\n");

            sb.append("🔍 **查找方式**\n");
            sb.append("  • 告诉我您想吃什么菜系\n");
            sb.append("  • 我会为您推荐附近的商家\n");
            sb.append("  • 例如：推荐附近的川菜馆");

            log.info("✅ [Tool] 查询附近商家成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询附近商家失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 估算两个位置之间的距离（简化版）
     * 实际应用中应该使用地图API
     */
    private int estimateDistance(String from, String to) {
        // 校园位置距离估算表（简化版）
        // 实际应该使用地图API计算真实距离

        if (from.contains("宿舍") && to.contains("食堂")) {
            return 300; // 宿舍到食堂约300米
        } else if (from.contains("宿舍") && to.contains("教学")) {
            return 600; // 宿舍到教学楼约600米
        } else if (from.contains("教学") && to.contains("食堂")) {
            return 400; // 教学楼到食堂约400米
        } else if (from.contains("图书馆") && to.contains("食堂")) {
            return 350; // 图书馆到食堂约350米
        } else if (from.contains("体育馆") && to.contains("食堂")) {
            return 500; // 体育馆到食堂约500米
        } else {
            // 默认距离
            return 500;
        }
    }
}

package com.xx.jaseatschoicejava.agent.tools.merchant;

import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商家统计工具类
 *
 * 为Agent提供商家统计分析功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class MerchantStatsTools {

    @Resource
    private MerchantService merchantService;

    /**
     * 获取商家统计数据
     *
     * @param merchantId 商家ID
     * @return 统计数据
     */
    @Tool("""
        获取商家的经营统计数据

        **统计数据包括：**
        - 评分信息
        - 人均消费
        - 商家分类

        **何时使用：**
        - 用户询问商家口碑
        - 选择优质商家
        - 对比商家质量

        **参数：** merchantId - 商家ID

        **返回：** 商家统计数据
        """)
    public String getMerchantStats(
        @P("商家ID") String merchantId
    ) {
        log.info("🔍 [Tool] 获取商家统计，merchantId: {}", merchantId);

        try {
            Merchant merchant = merchantService.getById(merchantId);

            if (merchant == null) {
                return "❌ 商家不存在";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📊 **%s** 经营统计\n\n",
                merchant.getName() != null ? merchant.getName() : "未命名商家"));

            sb.append("⭐ **评分信息**\n");
            double rating = merchant.getRating() != null ? merchant.getRating().doubleValue() : 0;
            sb.append(String.format("  • 商家评分：%.1f/5.0分\n\n", rating));

            sb.append("📈 **经营数据**\n");
            sb.append(String.format("  • 商家分类：%s\n",
                merchant.getCategory() != null ? merchant.getCategory() : "暂无"));
            sb.append(String.format("  • 人均消费：%.1f元\n\n",
                merchant.getAveragePrice() != null ? merchant.getAveragePrice() : 0));

            // 评级
            String level = rating >= 4.5 ? "优秀 🌟" :
                           rating >= 4.0 ? "良好 👍" :
                           rating >= 3.5 ? "中等 😐" : "需改善 😟";

            sb.append(String.format("🏆 **评级**：%s\n", level));

            if (rating >= 4.5) {
                sb.append("💡 该商家评分优秀，值得推荐");
            } else if (rating >= 4.0) {
                sb.append("💡 该商家评分良好，可以放心选择");
            }

            log.info("✅ [Tool] 获取商家统计成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取商家统计失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 对比商家数据
     *
     * @param merchantId1 商家1 ID
     * @param merchantId2 商家2 ID
     * @return 对比结果
     */
    @Tool("""
        对比两个商家的经营数据

        **对比维度：**
        - 评分对比
        - 人均消费对比
        - 综合推荐

        **何时使用：**
        - 用户在选择商家
        - 需要对比推荐

        **参数：**
        - merchantId1 - 商家1 ID
        - merchantId2 - 商家2 ID

        **返回：** 对比分析报告
        """)
    public String compareMerchants(
        @P("商家1 ID") String merchantId1,
        @P("商家2 ID") String merchantId2
    ) {
        log.info("🔍 [Tool] 对比商家，merchant1: {}, merchant2: {}", merchantId1, merchantId2);

        try {
            Merchant m1 = merchantService.getById(merchantId1);
            Merchant m2 = merchantService.getById(merchantId2);

            if (m1 == null || m2 == null) {
                return "❌ 商家不存在，无法对比";
            }

            double rating1 = m1.getRating() != null ? m1.getRating().doubleValue() : 0;
            double rating2 = m2.getRating() != null ? m2.getRating().doubleValue() : 0;
            double price1 = m1.getAveragePrice() != null ? m1.getAveragePrice().doubleValue() : 0;
            double price2 = m2.getAveragePrice() != null ? m2.getAveragePrice().doubleValue() : 0;

            StringBuilder sb = new StringBuilder();
            sb.append("📊 商家数据对比\n\n");
            sb.append(String.format("**%s** vs **%s**\n\n",
                m1.getName() != null ? m1.getName() : "商家1",
                m2.getName() != null ? m2.getName() : "商家2"));

            // 评分对比
            sb.append("⭐ **评分对比**\n");
            sb.append(String.format("  • %s：%.1f分\n",
                m1.getName() != null ? m1.getName() : "商家1", rating1));
            sb.append(String.format("  • %s：%.1f分\n",
                m2.getName() != null ? m2.getName() : "商家2", rating2));

            if (rating1 > rating2) {
                double diff = rating1 - rating2;
                sb.append(String.format("  → %s 评分更高 %.1f分 ✅\n\n",
                    m1.getName() != null ? m1.getName() : "商家1", diff));
            } else if (rating2 > rating1) {
                double diff = rating2 - rating1;
                sb.append(String.format("  → %s 评分更高 %.1f分 ✅\n\n",
                    m2.getName() != null ? m2.getName() : "商家2", diff));
            } else {
                sb.append("  → 评分相同\n\n");
            }

            // 人均消费对比
            sb.append("💰 **人均消费对比**\n");
            sb.append(String.format("  • %s：%.1f元\n",
                m1.getName() != null ? m1.getName() : "商家1", price1));
            sb.append(String.format("  • %s：%.1f元\n",
                m2.getName() != null ? m2.getName() : "商家2", price2));

            if (price1 < price2) {
                double diff = price2 - price1;
                sb.append(String.format("  → %s 人均消费更低 %.1f元 ✅\n\n",
                    m1.getName() != null ? m1.getName() : "商家1", diff));
            } else if (price2 < price1) {
                double diff = price1 - price2;
                sb.append(String.format("  → %s 人均消费更低 %.1f元 ✅\n\n",
                    m2.getName() != null ? m2.getName() : "商家2", diff));
            } else {
                sb.append("  → 人均消费相同\n\n");
            }

            // 综合建议
            sb.append("💡 **综合建议**\n");
            // 简化的评分算法：评分占70%，价格占30%（价格越低越好）
            double score1 = rating1 * 0.7 + (100 - price1) / 100 * 5 * 0.3;
            double score2 = rating2 * 0.7 + (100 - price2) / 100 * 5 * 0.3;

            if (score1 > score2) {
                sb.append(String.format("推荐选择 **%s**：综合评分更高\n",
                    m1.getName() != null ? m1.getName() : "商家1"));
            } else if (score2 > score1) {
                sb.append(String.format("推荐选择 **%s**：综合评分更高\n",
                    m2.getName() != null ? m2.getName() : "商家2"));
            } else {
                sb.append("两家商家综合评分相当，可以根据其他因素选择\n");
            }

            log.info("✅ [Tool] 对比商家成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 对比商家失败", e);
            return "❌ 对比失败：" + e.getMessage();
        }
    }

    /**
     * 获取商家排名
     *
     * @param limit 返回数量
     * @return 排名列表
     */
    @Tool("""
        获取商家排名列表

        **排名依据：**
        - 商家评分（主要）
        - 人均消费（次要）

        **何时使用：**
        - 展示优质商家
        - 推荐排行

        **参数：** limit - 返回数量（默认10）

        **返回：** 商家排名列表
        """)
    public String getMerchantRanking(
        @P("返回数量（默认10）") Integer limit
    ) {
        log.info("🔍 [Tool] 获取商家排名，limit: {}", limit);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            List<Merchant> merchants = merchantService.list();

            if (merchants.isEmpty()) {
                return "📋 暂无商家数据";
            }

            // 计算综合得分并排序
            merchants.sort((m1, m2) -> {
                double score1 = calculateOverallScore(m1);
                double score2 = calculateOverallScore(m2);
                return Double.compare(score2, score1);
            });

            // 限制返回数量
            if (merchants.size() > actualLimit) {
                merchants = merchants.subList(0, actualLimit);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🏆 商家排行榜（Top %d）\n\n", merchants.size()));

            for (int i = 0; i < merchants.size(); i++) {
                Merchant merchant = merchants.get(i);
                double overallScore = calculateOverallScore(merchant);

                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   ⭐ %.1f分 | 💰 人均%.1f元 | 📊 综合%.1f分\n\n",
                    i + 1,
                    merchant.getName() != null ? merchant.getName() : "未命名",
                    merchant.getRating() != null ? merchant.getRating() : 0,
                    merchant.getAveragePrice() != null ? merchant.getAveragePrice() : 0,
                    overallScore
                ));
            }

            sb.append("💡 排行基于评分和人均消费综合计算");

            log.info("✅ [Tool] 获取商家排名成功，数量: {}", merchants.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取商家排名失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 分析商家优势
     *
     * @param merchantId 商家ID
     * @return 优势分析
     */
    @Tool("""
        分析商家的优势和特色

        **分析维度：**
        - 评分优势
        - 价格优势
        - 改进建议

        **何时使用：**
        - 商家自我分析
        - 用户了解商家
        - 提升服务质量

        **参数：** merchantId - 商家ID

        **返回：** 优势分析报告
        """)
    public String analyzeMerchantAdvantages(
        @P("商家ID") String merchantId
    ) {
        log.info("🔍 [Tool] 分析商家优势，merchantId: {}", merchantId);

        try {
            Merchant merchant = merchantService.getById(merchantId);

            if (merchant == null) {
                return "❌ 商家不存在";
            }

            double rating = merchant.getRating() != null ? merchant.getRating().doubleValue() : 0;
            double averagePrice = merchant.getAveragePrice() != null ? merchant.getAveragePrice().doubleValue() : 0;

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("💪 **%s** 优势分析\n\n",
                merchant.getName() != null ? merchant.getName() : "未命名商家"));

            sb.append("🎯 **核心优势**\n");

            // 评分优势
            if (rating >= 4.5) {
                sb.append("  ⭐ 评分优秀：用户满意度极高\n");
            } else if (rating >= 4.0) {
                sb.append("  ⭐ 评分良好：用户满意度较高\n");
            } else if (rating >= 3.5) {
                sb.append("  ⭐ 评分中等：还有提升空间\n");
            } else {
                sb.append("  ⚠️ 评分偏低：需要改进服务质量\n");
            }

            // 价格优势
            if (averagePrice <= 15) {
                sb.append("  💰 价格实惠：性价比很高\n");
            } else if (averagePrice <= 25) {
                sb.append("  💰 价格适中：性价比较好\n");
            } else if (averagePrice <= 40) {
                sb.append("  💰 价格稍高：适合品质消费\n");
            } else {
                sb.append("  💰 价格高端：定位高端市场\n");
            }

            sb.append("\n💡 **改进建议**\n");

            if (rating < 4.0) {
                sb.append("  • 提升菜品质量和服务态度\n");
                sb.append("  • 关注用户评价，及时改进\n");
            }

            if (rating >= 4.5 && averagePrice <= 20) {
                sb.append("  • 保持优秀的服务质量和价格优势\n");
                sb.append("  • 继续听取用户建议，精益求精\n");
            }

            log.info("✅ [Tool] 分析商家优势成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 分析商家优势失败", e);
            return "❌ 分析失败：" + e.getMessage();
        }
    }

    /**
     * 计算综合得分
     */
    private double calculateOverallScore(Merchant merchant) {
        double rating = merchant.getRating() != null ? merchant.getRating().doubleValue() : 0;
        double averagePrice = merchant.getAveragePrice() != null ? merchant.getAveragePrice().doubleValue() : 30;

        // 评分占70%，价格占30%（价格越低分越高）
        double priceScore = Math.max(0, (50 - averagePrice) / 50 * 5);
        return rating * 0.7 + priceScore * 0.3;
    }
}

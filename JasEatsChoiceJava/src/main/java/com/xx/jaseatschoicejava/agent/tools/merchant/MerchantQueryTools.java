package com.xx.jaseatschoicejava.agent.tools.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商家查询工具类
 *
 * 为Agent提供商家信息查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class MerchantQueryTools {

    @Resource
    private MerchantService merchantService;

    /**
     * 获取商家基本信息
     *
     * @param merchantId 商家ID
     * @return 商家信息
     */
    @Tool("""
        获取商家的基本信息

        **返回信息：**
        - 商家名称
        - 联系电话
        - 商家地址
        - 人均消费
        - 商家分类

        **何时使用：**
        - 用户询问商家信息
        - 查看商家详情

        **参数：** merchantId - 商家ID

        **返回：** 商家基本信息
        """)
    public String getMerchantInfo(
        @P("商家ID") String merchantId
    ) {
        log.info("🔍 [Tool] 查询商家信息，merchantId: {}", merchantId);

        try {
            Merchant merchant = merchantService.getById(merchantId);

            if (merchant == null) {
                return "❌ 商家不存在";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🏪 商家信息\n\n");
            sb.append(String.format("**%s**\n\n", merchant.getName() != null ? merchant.getName() : "未命名商家"));

            sb.append("📋 **基本信息**\n");
            sb.append(String.format("  • 商家分类：%s\n", merchant.getCategory() != null ? merchant.getCategory() : "暂无"));
            sb.append(String.format("  • 联系电话：%s\n", merchant.getPhone() != null ? merchant.getPhone() : "暂无"));
            sb.append(String.format("  • 商家地址：%s\n\n", merchant.getAddress() != null ? merchant.getAddress() : "暂无"));

            sb.append("💰 **价格信息**\n");
            sb.append(String.format("  • 人均消费：%.1f元\n\n", merchant.getAveragePrice() != null ? merchant.getAveragePrice() : 0));

            sb.append("⭐ **评分信息**\n");
            sb.append(String.format("  • 商家评分：%.1f分\n", merchant.getRating() != null ? merchant.getRating() : 0));

            log.info("✅ [Tool] 查询商家信息成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询商家信息失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 搜索商家
     *
     * @param keyword 搜索关键词
     * @param limit 返回数量
     * @return 搜索结果
     */
    @Tool("""
        搜索商家

        **搜索范围：**
        - 商家名称
        - 商家分类

        **何时使用：**
        - 用户搜索特定商家
        - 按需求查找商家

        **参数：**
        - keyword - 搜索关键词
        - limit - 返回数量（默认10）

        **返回：** 搜索结果列表
        """)
    public String searchMerchants(
        @P("搜索关键词") String keyword,
        @P("返回数量（默认10）") Integer limit
    ) {
        log.info("🔍 [Tool] 搜索商家，keyword: {}, limit: {}", keyword, limit);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            // 使用名称模糊查询
            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<Merchant>()
                .like(Merchant::getName, keyword)
                .orderByDesc(Merchant::getRating)
                .last("LIMIT " + actualLimit);

            List<Merchant> merchants = merchantService.list(queryWrapper);

            if (merchants.isEmpty()) {
                return String.format("📋 未找到包含\"%s\"的商家", keyword);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔍 搜索结果（%d）\n\n", merchants.size()));

            for (int i = 0; i < merchants.size(); i++) {
                Merchant merchant = merchants.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   ⭐ %.1f分 | 📍 %s\n" +
                    "   💰 人均%.1f元 | %s\n\n",
                    i + 1,
                    merchant.getName() != null ? merchant.getName() : "未命名",
                    merchant.getRating() != null ? merchant.getRating() : 0,
                    merchant.getAddress() != null ? merchant.getAddress() : "地址暂无",
                    merchant.getAveragePrice() != null ? merchant.getAveragePrice() : 0,
                    merchant.getCategory() != null ? merchant.getCategory() : "未分类"
                ));
            }

            log.info("✅ [Tool] 搜索商家成功，数量: {}", merchants.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 搜索商家失败", e);
            return "❌ 搜索失败：" + e.getMessage();
        }
    }

    /**
     * 获取热门商家
     *
     * @param limit 返回数量
     * @return 热门商家列表
     */
    @Tool("""
        获取当前热门商家

        **热度依据：**
        - 商家评分
        - 人气程度

        **何时使用：**
        - 用户询问热门推荐
        - 首页展示

        **参数：** limit - 返回数量（默认10）

        **返回：** 热门商家列表
        """)
    public String getHotMerchants(
        @P("返回数量（默认10）") Integer limit
    ) {
        log.info("🔍 [Tool] 获取热门商家，limit: {}", limit);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<Merchant>()
                .isNotNull(Merchant::getRating)
                .ge(Merchant::getRating, 4.0)
                .orderByDesc(Merchant::getRating)
                .last("LIMIT " + actualLimit);

            List<Merchant> merchants = merchantService.list(queryWrapper);

            if (merchants.isEmpty()) {
                return "📋 暂无热门商家";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔥 热门商家（Top %d）\n\n", merchants.size()));

            for (int i = 0; i < merchants.size(); i++) {
                Merchant merchant = merchants.get(i);
                sb.append(String.format(
                    "%d. **%s** ⭐ %.1f分\n" +
                    "   💰 人均%.1f元 | %s\n" +
                    "   📍 %s\n\n",
                    i + 1,
                    merchant.getName() != null ? merchant.getName() : "未命名",
                    merchant.getRating() != null ? merchant.getRating() : 0,
                    merchant.getAveragePrice() != null ? merchant.getAveragePrice() : 0,
                    merchant.getCategory() != null ? merchant.getCategory() : "未分类",
                    merchant.getAddress() != null ? merchant.getAddress() : "地址暂无"
                ));
            }

            log.info("✅ [Tool] 获取热门商家成功，数量: {}", merchants.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取热门商家失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 按评分筛选商家
     *
     * @param minRating 最低评分
     * @param limit 返回数量
     * @return 商家列表
     */
    @Tool("""
        按评分筛选商家

        **何时使用：**
        - 用户要求高质量商家
        - 筛选高评分店铺

        **参数：**
        - minRating - 最低评分（0-5）
        - limit - 返回数量（默认10）

        **返回：** 符合评分要求的商家列表
        """)
    public String filterMerchantsByRating(
        @P("最低评分（0-5）") Double minRating,
        @P("返回数量（默认10）") Integer limit
    ) {
        log.info("🔍 [Tool] 按评分筛选商家，minRating: {}, limit: {}", minRating, limit);

        try {
            if (minRating == null || minRating < 0 || minRating > 5) {
                return "⚠️ 请输入有效的评分（0-5之间）";
            }

            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<Merchant>()
                .isNotNull(Merchant::getRating)
                .ge(Merchant::getRating, minRating)
                .orderByDesc(Merchant::getRating)
                .last("LIMIT " + actualLimit);

            List<Merchant> merchants = merchantService.list(queryWrapper);

            if (merchants.isEmpty()) {
                return String.format("📋 暂无评分≥%.1f的商家", minRating);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("⭐ 高评分商家（≥%.1f分）\n\n", minRating));

            for (int i = 0; i < merchants.size(); i++) {
                Merchant merchant = merchants.get(i);
                sb.append(String.format(
                    "%d. **%s** ⭐ %.1f分\n" +
                    "   💰 人均%.1f元 | %s\n" +
                    "   📍 %s\n\n",
                    i + 1,
                    merchant.getName() != null ? merchant.getName() : "未命名",
                    merchant.getRating() != null ? merchant.getRating() : 0,
                    merchant.getAveragePrice() != null ? merchant.getAveragePrice() : 0,
                    merchant.getCategory() != null ? merchant.getCategory() : "未分类",
                    merchant.getAddress() != null ? merchant.getAddress() : "地址暂无"
                ));
            }

            sb.append(String.format("💡 共找到%d家高评分商家", merchants.size()));

            log.info("✅ [Tool] 评分筛选完成，数量: {}", merchants.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 评分筛选失败", e);
            return "❌ 筛选失败：" + e.getMessage();
        }
    }

    /**
     * 比较两个商家
     *
     * @param merchantId1 商家1 ID
     * @param merchantId2 商家2 ID
     * @return 对比结果
     */
    @Tool("""
        比较两个商家的差异

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
        log.info("🔍 [Tool] 比较商家，merchant1: {}, merchant2: {}", merchantId1, merchantId2);

        try {
            Merchant m1 = merchantService.getById(merchantId1);
            Merchant m2 = merchantService.getById(merchantId2);

            if (m1 == null || m2 == null) {
                return "❌ 商家不存在，无法比较";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📊 商家对比分析\n\n");
            sb.append(String.format("**%s** vs **%s**\n\n",
                m1.getName() != null ? m1.getName() : "商家1",
                m2.getName() != null ? m2.getName() : "商家2"));

            // 评分对比
            double rating1 = m1.getRating() != null ? m1.getRating().doubleValue() : 0;
            double rating2 = m2.getRating() != null ? m2.getRating().doubleValue() : 0;
            sb.append("⭐ **评分对比**\n");
            sb.append(String.format("  • %s：%.1f分\n", m1.getName() != null ? m1.getName() : "商家1", rating1));
            sb.append(String.format("  • %s：%.1f分\n", m2.getName() != null ? m2.getName() : "商家2", rating2));

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
            double price1 = m1.getAveragePrice() != null ? m1.getAveragePrice().doubleValue() : 0;
            double price2 = m2.getAveragePrice() != null ? m2.getAveragePrice().doubleValue() : 0;
            sb.append("💰 **人均消费对比**\n");
            sb.append(String.format("  • %s：%.1f元\n", m1.getName() != null ? m1.getName() : "商家1", price1));
            sb.append(String.format("  • %s：%.1f元\n", m2.getName() != null ? m2.getName() : "商家2", price2));

            if (price1 < price2) {
                sb.append(String.format("  → %s 人均消费更低 %.1f元 ✅\n\n",
                    m1.getName() != null ? m1.getName() : "商家1", price2 - price1));
            } else if (price2 < price1) {
                sb.append(String.format("  → %s 人均消费更低 %.1f元 ✅\n\n",
                    m2.getName() != null ? m2.getName() : "商家2", price1 - price2));
            } else {
                sb.append("  → 人均消费相同\n\n");
            }

            // 综合建议
            sb.append("💡 **综合建议**\n");
            if (rating1 > rating2 && price1 <= price2) {
                sb.append(String.format("推荐选择 **%s**：评分更高且价格更实惠\n",
                    m1.getName() != null ? m1.getName() : "商家1"));
            } else if (rating2 > rating1 && price2 <= price1) {
                sb.append(String.format("推荐选择 **%s**：评分更高且价格更实惠\n",
                    m2.getName() != null ? m2.getName() : "商家2"));
            } else if (rating1 > rating2) {
                sb.append(String.format("如果更看重质量，推荐 **%s**\n",
                    m1.getName() != null ? m1.getName() : "商家1"));
            } else if (rating2 > rating1) {
                sb.append(String.format("如果更看重质量，推荐 **%s**\n",
                    m2.getName() != null ? m2.getName() : "商家2"));
            } else {
                sb.append("两家商家各有优势，可以根据具体需求选择\n");
            }

            log.info("✅ [Tool] 商家对比完成");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 商家对比失败", e);
            return "❌ 对比失败：" + e.getMessage();
        }
    }
}

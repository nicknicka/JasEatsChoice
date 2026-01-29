package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.HotTopicResponse;
import com.xx.jaseatschoicejava.entity.HotTopic;

/**
 * 今日热点服务接口
 */
public interface HotTopicService extends IService<HotTopic> {

    /**
     * 获取今日热点（组合策略）
     * 优先级：数据库配置 > 热门教程 > AI生成 > 默认热点
     *
     * @return 热点响应对象
     */
    HotTopicResponse getTodayHotTopic();

    /**
     * 从数据库查询当前生效的热点
     *
     * @return 热点实体，如果没有则返回null
     */
    HotTopic getActiveFromDatabase();

    /**
     * 从热门教程提取热点
     *
     * @return 热点内容，如果没有则返回null
     */
    String extractFromTutorial();

    /**
     * AI生成热点内容
     *
     * @return 热点内容，如果生成失败则返回null
     */
    String generateByAI();

    /**
     * 从第三方API获取热点
     *
     * @return 热点内容，如果获取失败则返回null
     */
    String fetchFromThirdPartyAPI();

    /**
     * 记录热点点击
     *
     * @param content 热点内容
     */
    void recordClick(String content);

    /**
     * 记录热点分享
     *
     * @param content 热点内容
     */
    void recordShare(String content);

    /**
     * 创建热点（管理员功能）
     *
     * @param hotTopic 热点实体
     * @return 是否创建成功
     */
    boolean createHotTopic(HotTopic hotTopic);

    /**
     * 更新热点（管理员功能）
     *
     * @param hotTopic 热点实体
     * @return 是否更新成功
     */
    boolean updateHotTopic(HotTopic hotTopic);

    /**
     * 删除热点（管理员功能）
     *
     * @param id 热点ID
     * @return 是否删除成功
     */
    boolean deleteHotTopic(String id);

    /**
     * 审核热点（管理员功能）
     *
     * @param id          热点ID
     * @param reviewerId  审核人ID
     * @param approved    是否通过
     * @param comment     审核意见
     * @return 是否审核成功
     */
    boolean reviewHotTopic(String id, Long reviewerId, boolean approved, String comment);
}

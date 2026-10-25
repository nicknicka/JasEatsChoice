package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.HotTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 今日热点Mapper接口
 */
@Mapper
public interface HotTopicMapper extends BaseMapper<HotTopic> {

    /**
     * 查询当前生效的热点（按优先级排序）
     *
     * @param now 当前时间
     * @return 生效的热点列表
     */
    @Select("SELECT * FROM hot_topic " +
            "WHERE status = 'ACTIVE' " +
            "AND (start_date IS NULL OR start_date <= #{now}) " +
            "AND (end_date IS NULL OR end_date >= #{now}) " +
            "AND (require_review = FALSE OR review_status = 'APPROVED') " +
            "ORDER BY priority DESC, create_time DESC " +
            "LIMIT 1")
    HotTopic selectActiveHotTopic(LocalDateTime now);

    /**
     * 查询最新的N个热点（用于管理后台）
     *
     * @return 热点列表
     */
    @Select("SELECT * FROM hot_topic " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<HotTopic> selectRecentHotTopics(int limit);

    /**
     * 统计点击次数
     *
     * @param id 热点ID
     */
    @Select("UPDATE hot_topic SET click_count = click_count + 1 WHERE id = #{id}")
    void incrementClickCount(String id);

    /**
     * 统计分享次数
     *
     * @param id 热点ID
     */
    @Select("UPDATE hot_topic SET share_count = share_count + 1 WHERE id = #{id}")
    void incrementShareCount(String id);
}

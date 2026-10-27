package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Tutorial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TutorialMapper extends BaseMapper<Tutorial> {
    // 自定义查询：获取首页推荐的教程
    List<Tutorial> selectFeaturedTutorials();

    /**
     * 查询最热门的教程（按浏览量排序）
     *
     * @return 最热门的教程
     */
    Tutorial selectHottestTutorial();

    /**
     * 查询教程统计概览。
     *
     * @return 总数、发布数、待审数、浏览数、收藏数和平均评分
     */
    @Select("SELECT COUNT(*) AS totalTutorials, " +
            "COALESCE(SUM(CASE WHEN status = 'PUBLISHED' THEN 1 ELSE 0 END), 0) AS publishedTutorials, " +
            "COALESCE(SUM(CASE WHEN review_status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingReview, " +
            "COALESCE(SUM(view_count), 0) AS totalViews, " +
            "COALESCE(SUM(favorite_count), 0) AS totalFavorites, " +
            "COALESCE(AVG(rating), 0) AS averageRating " +
            "FROM cnt_tutorial")
    Map<String, Object> selectStatisticsOverview();
}

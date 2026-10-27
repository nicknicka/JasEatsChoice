package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.FestivalDishRecommend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 节日推荐菜品Mapper接口
 *

 * @since 2025-01-31
 */
@Mapper
public interface FestivalDishRecommendMapper extends BaseMapper<FestivalDishRecommend> {

    /**
     * 查询节日推荐菜品（包含菜品信息）
     *
     * @param festivalId 节日ID
     * @return 推荐菜品列表
     */
    @Select("SELECT fdr.*, d.name AS dish_name, d.image AS dish_image, d.price " +
            "FROM cnt_festival_dish_recommend fdr " +
            "LEFT JOIN mch_dish d ON fdr.dish_id = d.id " +
            "WHERE fdr.festival_id = #{festivalId} AND fdr.is_active = 1 " +
            "ORDER BY fdr.priority DESC, fdr.order_count DESC")
    List<FestivalDishRecommend> selectByFestivalIdWithDish(@Param("festivalId") String festivalId);

    /**
     * 查询用户首页推荐菜品
     *
     * @param festivalIds 节日ID列表
     * @param position 展示位置
     * @param limit 限制数量
     * @return 推荐菜品列表
     */
    @Select("SELECT fdr.*, d.name AS dish_name, d.image AS dish_image, d.price " +
            "FROM cnt_festival_dish_recommend fdr " +
            "LEFT JOIN mch_dish d ON fdr.dish_id = d.id " +
            "WHERE fdr.festival_id IN " +
            "<foreach collection='festivalIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND fdr.position = #{position} AND fdr.is_active = 1 " +
            "ORDER BY fdr.priority DESC, fdr.order_count DESC " +
            "LIMIT #{limit}")
    List<FestivalDishRecommend> selectHomepageRecommend(@Param("festivalIds") List<String> festivalIds,
                                                        @Param("position") Integer position,
                                                        @Param("limit") Integer limit);

    /**
     * 根据节日和菜品查询推荐记录
     *
     * @param festivalId 节日ID
     * @param dishId 菜品ID
     * @return 推荐记录
     */
    @Select("SELECT * FROM cnt_festival_dish_recommend " +
            "WHERE festival_id = #{festivalId} AND dish_id = #{dishId} LIMIT 1")
    FestivalDishRecommend selectByFestivalAndDish(@Param("festivalId") String festivalId,
                                                  @Param("dishId") String dishId);

    /**
     * 按推荐记录ID累加反馈计数
     *
     * @param id 推荐记录ID
     * @param clickIncrement 点击增量
     * @param orderIncrement 下单增量
     * @return 更新行数
     */
    @Update("UPDATE cnt_festival_dish_recommend " +
            "SET click_count = IFNULL(click_count, 0) + #{clickIncrement}, " +
            "order_count = IFNULL(order_count, 0) + #{orderIncrement} " +
            "WHERE id = #{id}")
    int incrementFeedbackCounts(@Param("id") String id,
                                @Param("clickIncrement") int clickIncrement,
                                @Param("orderIncrement") int orderIncrement);
}

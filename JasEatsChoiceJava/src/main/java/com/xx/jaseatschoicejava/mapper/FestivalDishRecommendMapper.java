package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.FestivalDishRecommend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 节日推荐菜品Mapper接口
 *
 * @author Claude
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
    @Select("SELECT fdr.*, d.dish_name, d.dish_image, d.price " +
            "FROM t_festival_dish_recommend fdr " +
            "LEFT JOIN t_dish d ON fdr.dish_id = d.id " +
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
    @Select("SELECT fdr.*, d.dish_name, d.dish_image, d.price " +
            "FROM t_festival_dish_recommend fdr " +
            "LEFT JOIN t_dish d ON fdr.dish_id = d.id " +
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
}

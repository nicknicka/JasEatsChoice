package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Festival;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 节日Mapper接口
 *
 * @author Claude
 * @since 2025-01-31
 */
@Mapper
public interface FestivalMapper extends BaseMapper<Festival> {

    /**
     * 查询当前生效的节日列表
     *
     * @return 节日列表
     */
    @Select("SELECT * FROM v_active_festivals WHERE is_current = 1 ORDER BY sort_order DESC")
    List<Festival> selectActiveFestivals();

    /**
     * 查询即将到来的节日（指定天数内）
     *
     * @param days 天数
     * @return 节日列表
     */
    @Select("SELECT * FROM v_active_festivals WHERE days_until_festival >= 0 AND days_until_festival <= #{days} ORDER BY days_until_festival ASC")
    List<Festival> selectUpcomingFestivals(@Param("days") int days);

    /**
     * 根据类型查询节日
     *
     * @param festivalType 节日类型
     * @return 节日列表
     */
    @Select("SELECT * FROM t_festival WHERE festival_type = #{festivalType} AND is_active = 1 ORDER BY sort_order DESC")
    List<Festival> selectByType(@Param("festivalType") String festivalType);
}

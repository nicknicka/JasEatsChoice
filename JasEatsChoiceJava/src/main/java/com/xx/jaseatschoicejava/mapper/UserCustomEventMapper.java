package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.UserCustomEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户自定义事件Mapper接口
 *
 * @author Claude
 * @since 2025-01-31
 */
@Mapper
public interface UserCustomEventMapper extends BaseMapper<UserCustomEvent> {

    /**
     * 查询用户的自定义事件列表
     *
     * @param userId 用户ID
     * @return 事件列表
     */
    @Select("SELECT * FROM t_user_custom_event WHERE user_id = #{userId} AND is_active = 1 ORDER BY create_time DESC")
    List<UserCustomEvent> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户即将到来的事件
     *
     * @param userId 用户ID
     * @param days 天数范围
     * @return 事件列表
     */
    @Select("SELECT * FROM t_user_custom_event " +
            "WHERE user_id = #{userId} AND is_active = 1 " +
            "AND DATEDIFF(CONCAT(IFNULL(year, YEAR(CURDATE())), '-', event_date), CURDATE()) BETWEEN 0 AND #{days} " +
            "ORDER BY event_date ASC")
    List<UserCustomEvent> selectUpcomingEvents(@Param("userId") String userId, @Param("days") int days);
}

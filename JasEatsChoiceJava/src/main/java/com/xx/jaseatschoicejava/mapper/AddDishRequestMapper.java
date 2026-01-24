package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.AddDishRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 加菜请求Mapper
 */
@Mapper
public interface AddDishRequestMapper extends BaseMapper<AddDishRequest> {

    /**
     * 查询超时的待审核请求
     */
    @Select("SELECT * FROM t_add_dish_request " +
            "WHERE approval_status = 0 " +
            "AND timeout_time < #{now}")
    List<AddDishRequest> selectTimeoutRequests(@Param("now") LocalDateTime now);

    /**
     * 查询需要提醒的请求（10分钟未处理且未发送二次提醒）
     */
    @Select("SELECT * FROM t_add_dish_request " +
            "WHERE approval_status = 0 " +
            "AND first_remind_time < #{now} " +
            "AND second_remind_time IS NULL")
    List<AddDishRequest> selectNeedRemindRequests(@Param("now") LocalDateTime now);
}

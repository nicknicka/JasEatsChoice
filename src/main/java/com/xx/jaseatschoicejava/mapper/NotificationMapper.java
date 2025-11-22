package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Notification;

/**
 * 通知Mapper接口
 */


@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}

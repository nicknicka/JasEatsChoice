package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.AdminOperationLog;

/**
 * 管理员操作日志Mapper接口
 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}

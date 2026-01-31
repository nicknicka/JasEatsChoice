package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.AdminOperationLog;
import com.xx.jaseatschoicejava.mapper.AdminOperationLogMapper;
import com.xx.jaseatschoicejava.service.AdminOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 管理员操作日志服务实现
 */
@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog> implements AdminOperationLogService {
}

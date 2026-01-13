package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.ConsumeHistory;
import com.xx.jaseatschoicejava.mapper.ConsumeHistoryMapper;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ConsumeHistoryServiceImpl extends ServiceImpl<ConsumeHistoryMapper, ConsumeHistory> implements ConsumeHistoryService {

    private final ConsumeHistoryMapper consumeHistoryMapper;

    @Override
    public Page<ConsumeHistory> getConsumeHistoryByUserId(Long userId, String type, String startDate, String endDate, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ConsumeHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeHistory::getUserId, userId);

        // 类型筛选
        if (type != null && !type.equals("all")) {
            queryWrapper.eq(ConsumeHistory::getType, type);
        }

        // 日期范围筛选
        if (startDate != null) {
            LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
            queryWrapper.ge(ConsumeHistory::getCreateTime, startDateTime);
        }

        if (endDate != null) {
            LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            queryWrapper.le(ConsumeHistory::getCreateTime, endDateTime);
        }

        // 按时间倒序
        queryWrapper.orderByDesc(ConsumeHistory::getCreateTime);

        // 分页查询
        return consumeHistoryMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }
}

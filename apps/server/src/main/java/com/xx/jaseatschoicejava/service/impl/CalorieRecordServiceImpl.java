package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.CalorieRecord;
import com.xx.jaseatschoicejava.mapper.CalorieRecordMapper;
import com.xx.jaseatschoicejava.service.CalorieRecordService;
import org.springframework.stereotype.Service;

/**
 * 卡路里记录服务实现
 */
@Service
public class CalorieRecordServiceImpl extends ServiceImpl<CalorieRecordMapper, CalorieRecord> implements CalorieRecordService {
}

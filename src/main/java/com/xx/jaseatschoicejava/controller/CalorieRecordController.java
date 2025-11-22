package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.CalorieRecord;
import com.xx.jaseatschoicejava.service.CalorieRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡路里记录控制器
 */
@RestController
@RequestMapping("/calorie-records")
public class CalorieRecordController {

    @Autowired
    private CalorieRecordService calorieRecordService;

    /**
     * 创建卡路里记录
     */
    @PostMapping
    public ResponseResult<?> createCalorieRecord(@RequestBody CalorieRecord record) {
        boolean success = calorieRecordService.save(record);
        if (success) {
            return ResponseResult.success(record.getId());
        }
        return ResponseResult.fail("500", "创建记录失败");
    }

    /**
     * 根据用户ID获取卡路里记录
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<?> getRecordsByUserId(@PathVariable Long userId) {
        LambdaQueryWrapper<CalorieRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalorieRecord::getUserId, userId);
        queryWrapper.orderByDesc(CalorieRecord::getRecordTime); // 按记录时间降序排列
        List<CalorieRecord> records = calorieRecordService.list(queryWrapper);
        return ResponseResult.success(records);
    }
}

package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.CalorieRecord;
import com.xx.jaseatschoicejava.service.CalorieRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

/**
 * 卡路里记录控制器
 */
@Slf4j
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
    public ResponseResult<?> getRecordsByUserId(@PathVariable String userId) {
        LambdaQueryWrapper<CalorieRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalorieRecord::getUserId, userId);
        queryWrapper.orderByDesc(CalorieRecord::getRecordTime); // 按记录时间降序排列
        List<CalorieRecord> records = calorieRecordService.list(queryWrapper);
        return ResponseResult.success(records);
    }

    /**
     * 根据用户ID获取本周卡路里记录统计
     */
    @GetMapping("/user/{userId}/week")
    public ResponseResult<?> getWeeklyRecordsByUserId(@PathVariable String userId) {
        LambdaQueryWrapper<CalorieRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalorieRecord::getUserId, userId);

        // 计算当前周的开始和结束日期
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1-7，周一到周日
        LocalDate startOfWeek = today.minusDays(dayOfWeek - 1); // 周一
        LocalDateTime startTime = startOfWeek.atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(6).atTime(LocalTime.MAX); // 周日23:59:59

        // 查询本周的所有记录
        queryWrapper.between(CalorieRecord::getRecordTime, startTime, endOfWeek);

        List<CalorieRecord> records = calorieRecordService.list(queryWrapper);

        // 初始化每周七天的数据
        Map<String, Map<String, Object>> weeklyDataMap = new HashMap<>();
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (String day : weekDays) {
            weeklyDataMap.put(day, Map.of("day", day, "consumed", 0));
        }

        // 计算每天的总卡路里
        for (CalorieRecord record : records) {
            int recordDayOfWeek = record.getRecordTime().getDayOfWeek().getValue();
            String day = weekDays[recordDayOfWeek - 1];
            Map<String, Object> dayData = (Map<String, Object>) weeklyDataMap.get(day);
            int currentConsumed = (Integer) dayData.get("consumed");
            weeklyDataMap.put(day, Map.of("day", day, "consumed", currentConsumed + record.getCalorie()));
        }

        // 转换为列表并按顺序返回
        List<Map<String, Object>> weeklyDataList = Arrays.stream(weekDays)
            .map(weeklyDataMap::get)
            .collect(Collectors.toList());

        log.info("每周卡路里数据: {}", weeklyDataList);
        return ResponseResult.success(weeklyDataList);
    }

    /**
     * 根据用户ID和日期获取卡路里记录
     */
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseResult<?> getRecordsByUserIdAndDate(@PathVariable String userId, @PathVariable String date) {
        LambdaQueryWrapper<CalorieRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalorieRecord::getUserId, userId);

        // 解析日期并查询该日期范围内的记录
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startTime = localDate.atStartOfDay(); // 当天00:00:00
        LocalDateTime endTime = localDate.atTime(LocalTime.MAX); // 当天23:59:59

        queryWrapper.between(CalorieRecord::getRecordTime, startTime, endTime);
        queryWrapper.orderByAsc(CalorieRecord::getRecordTime); // 按记录时间升序排列
        List<CalorieRecord> records = calorieRecordService.list(queryWrapper);
        return ResponseResult.success(records);
    }

    /**
     * 获取今日营养摄入统计
     */
    @GetMapping("/user/{userId}/today-summary")
    public ResponseResult<?> getTodayNutritionSummary(@PathVariable String userId) {
        LambdaQueryWrapper<CalorieRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CalorieRecord::getUserId, userId);

        // 查询今日的所有记录
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay(); // 当天00:00:00
        LocalDateTime endTime = today.atTime(LocalTime.MAX); // 当天23:59:59
        queryWrapper.between(CalorieRecord::getRecordTime, startTime, endTime);

        List<CalorieRecord> records = calorieRecordService.list(queryWrapper);

        // 计算营养总和
        double totalCalorie = records.stream().mapToInt(CalorieRecord::getCalorie).sum();
        double totalProtein = records.stream().mapToDouble(record -> record.getProtein() != null ? record.getProtein() : 0).sum();
        double totalFat = records.stream().mapToDouble(record -> record.getFat() != null ? record.getFat() : 0).sum();
        double totalCarbohydrate = records.stream().mapToDouble(record -> record.getCarbohydrate() != null ? record.getCarbohydrate() : 0).sum();

        // 构建结果
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCalorie", totalCalorie);
        summary.put("totalProtein", totalProtein);
        summary.put("totalFat", totalFat);
        summary.put("totalCarbohydrate", totalCarbohydrate);

        return ResponseResult.success(summary);
    }

    /**
     * 更新卡路里记录
     */
    @PutMapping
    public ResponseResult<?> updateCalorieRecord(@RequestBody CalorieRecord record) {
        log.info("更新记录: {}", record);

        // 检查记录是否存在
        if (record.getId() == null) {
            log.error("更新失败，记录ID为空");
            return ResponseResult.fail("400", "记录ID不能为空");
        }

        boolean exists = calorieRecordService.getById(record.getId()) != null;
        if (!exists) {
            log.error("更新失败，记录不存在，ID: {}", record.getId());
            return ResponseResult.fail("404", "记录不存在");
        }

        boolean updated = calorieRecordService.updateById(record);

        // 检查是否真的更新了数据
        if (updated) {
            log.info("更新记录成功，ID: {}", record.getId());
            return ResponseResult.success("更新记录成功");
        }

        // 没有更新任何行的情况，可能是字段未变化
        log.info("更新记录已完成，数据未变化，ID: {}", record.getId());
        return ResponseResult.success("数据未变化");

    }

    /**
     * 删除卡路里记录
     */
    @DeleteMapping("/{recordId}")
    public ResponseResult<?> deleteCalorieRecord(@PathVariable String recordId) {
        log.info("删除记录，ID: {}", recordId);

        // 检查记录是否存在
        CalorieRecord record = calorieRecordService.getById(recordId);
        if (record == null) {
            log.error("删除失败，记录不存在，ID: {}", recordId);
            return ResponseResult.fail("404", "记录不存在");
        }

        boolean success = calorieRecordService.removeById(recordId);
        if (success) {
            log.info("删除记录成功，ID: {}", recordId);
            return ResponseResult.success("删除成功");
        }

        log.error("删除记录失败，ID: {}", recordId);
        return ResponseResult.fail("500", "删除失败");
    }
}

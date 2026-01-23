package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.CalorieRecord;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.CalorieRecordService;
import com.xx.jaseatschoicejava.service.UserContextService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用户上下文服务实现类
 * 负责构建AI聊天时需要的用户个性化信息上下文
 */
@Slf4j
@Service
public class UserContextServiceImpl implements UserContextService {

    @Resource
    private UserService userService;

    @Resource
    private CalorieRecordService calorieRecordService;

    @Resource
    private WeatherService weatherService;

    @Override
    public String buildUserContext(String userId, boolean enablePersonalData) {
        // 如果未启用个人数据，返回空字符串
        if (!enablePersonalData) {
            return "";
        }

        try {
            StringBuilder context = new StringBuilder();
            context.append("\n\n【用户个人信息】\n");

            // 获取用户基本信息
            User user = userService.getById(userId);
            if (user != null) {
                // 基本信息
                context.append("- 身高：")
                        .append(user.getHeight() != null ? user.getHeight() + "cm" : "未设置")
                        .append("\n");
                context.append("- 体重：")
                        .append(user.getWeight() != null ? user.getWeight() + "kg" : "未设置")
                        .append("\n");
                context.append("- 饮食目标：")
                        .append(user.getDietGoal() != null ? user.getDietGoal() : "未设置")
                        .append("\n");

                // 过敏信息
                if (user.getAllergies() != null) {
                    String allergiesStr = user.getAllergies().toString();
                    context.append("- 过敏食材：").append(allergiesStr).append("\n");
                }

                // 偏好标签
                if (user.getPreferTags() != null) {
                    String preferTagsStr = user.getPreferTags().toString();
                    context.append("- 饮食偏好：").append(preferTagsStr).append("\n");
                }

                // 计算BMI
                if (user.getHeight() != null && user.getWeight() != null) {
                    double heightInMeters = user.getHeight() / 100.0;
                    double bmi = user.getWeight() / (heightInMeters * heightInMeters);
                    context.append(String.format("- BMI：%.1f\n", bmi));
                }
            }

            // 今日饮食统计
            context.append("\n【今日饮食统计】\n");
            Map<String, Object> todayStats = getTodayNutritionSummary(userId);
            if (todayStats != null && !todayStats.isEmpty()) {
                context.append("- 已摄入卡路里：").append(todayStats.get("calorie")).append(" kcal\n");
                context.append("- 蛋白质：").append(todayStats.get("protein")).append("g\n");
                context.append("- 脂肪：").append(todayStats.get("fat")).append("g\n");
                context.append("- 碳水化合物：").append(todayStats.get("carbohydrate")).append("g\n");
            } else {
                context.append("暂无今日饮食记录\n");
            }

            // 当前天气
            if (user != null && user.getLocation() != null) {
                context.append("\n【当前天气】\n");
                try {
                    Map<String, Object> weather = weatherService.getWeatherInfo(user.getLocation());
                    if (weather != null && !weather.isEmpty()) {
                        context.append("- 所在地：").append(user.getLocation()).append("\n");
                        context.append("- 天气：").append(weather.get("condition")).append("\n");
                        context.append("- 温度：").append(weather.get("temperature")).append("°C\n");
                        context.append("- 湿度：").append(weather.get("humidity")).append("%\n");
                    } else {
                        context.append("天气信息暂不可用\n");
                    }
                } catch (Exception e) {
                    log.warn("获取天气信息失败: {}", e.getMessage());
                    context.append("天气信息获取失败\n");
                }
            }

            context.append("\n请根据以上信息，为用户提供个性化的饮食建议。\n");
            context.append("注意：\n");
            context.append("1. 考虑用户的饮食目标和过敏信息，避免推荐过敏食材\n");
            context.append("2. 结合今日已摄入的营养成分给出合理的饮食建议\n");
            context.append("3. 参考天气情况推荐合适的食物\n");

            log.info("成功构建用户上下文，userId: {}, context length: {}", userId, context.length());
            return context.toString();

        } catch (Exception e) {
            log.error("构建用户上下文失败: userId={}, error={}", userId, e.getMessage(), e);
            return "";  // 出错时返回空字符串，避免影响AI正常对话
        }
    }

    /**
     * 获取今日营养摄入汇总
     */
    private Map<String, Object> getTodayNutritionSummary(String userId) {
        try {
            // 查询今天的所有记录
            LambdaQueryWrapper<CalorieRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CalorieRecord::getUserId, userId);
            queryWrapper.ge(CalorieRecord::getRecordTime, LocalDate.now().atStartOfDay());
            queryWrapper.lt(CalorieRecord::getRecordTime, LocalDate.now().plusDays(1).atStartOfDay());

            List<CalorieRecord> todayRecords = calorieRecordService.list(queryWrapper);

            if (todayRecords == null || todayRecords.isEmpty()) {
                return null;
            }

            // 汇总营养数据
            int totalCalorie = 0;
            double totalProtein = 0.0;
            double totalFat = 0.0;
            double totalCarbohydrate = 0.0;

            for (CalorieRecord record : todayRecords) {
                totalCalorie += record.getCalorie();
                if (record.getProtein() != null) {
                    totalProtein += record.getProtein();
                }
                if (record.getFat() != null) {
                    totalFat += record.getFat();
                }
                if (record.getCarbohydrate() != null) {
                    totalCarbohydrate += record.getCarbohydrate();
                }
            }

            Map<String, Object> summary = Map.of(
                    "calorie", totalCalorie,
                    "protein", String.format("%.1f", totalProtein),
                    "fat", String.format("%.1f", totalFat),
                    "carbohydrate", String.format("%.1f", totalCarbohydrate)
            );

            log.debug("今日营养汇总: userId={}, 总卡路里={}", userId, totalCalorie);
            return summary;

        } catch (Exception e) {
            log.error("获取今日营养汇总失败: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }
}

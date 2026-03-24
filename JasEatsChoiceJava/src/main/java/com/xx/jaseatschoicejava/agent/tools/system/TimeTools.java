package com.xx.jaseatschoicejava.agent.tools.system;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间服务工具类
 *
 * 为Agent提供时间相关功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class TimeTools {

    /**
     * 获取当前时间
     *
     * @return 当前时间信息
     */
    @Tool("""
        获取当前时间和日期

        **返回格式：**
        - 日期：YYYY-MM-DD
        - 时间：HH:mm
        - 星期：星期一 ~ 星期日
        - 时段：早晨/上午/中午/下午/晚上/深夜

        **何时使用：**
        - 推荐早午晚餐
        - 判断商家营业状态
        - 显示时间相关信息

        **返回：** 当前时间信息
        """)
    public String getCurrentTime() {
        log.info("🔍 [Tool] 获取当前时间");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDate date = now.toLocalDate();
            LocalTime time = now.toLocalTime();
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String period = getTimePeriod(time);

            StringBuilder sb = new StringBuilder();
            sb.append("🕐 当前时间\n\n");
            sb.append(String.format("  • 日期：%s\n", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            sb.append(String.format("  • 时间：%s\n", time.format(DateTimeFormatter.ofPattern("HH:mm"))));
            sb.append(String.format("  • 星期：%s\n", formatDayOfWeek(dayOfWeek)));
            sb.append(String.format("  • 时段：%s\n", period));

            // 添加时段建议
            sb.append("\n💡 ");
            switch (period) {
                case "早晨":
                    sb.append("早餐时间，推荐营养丰富的早餐");
                    break;
                case "上午":
                    sb.append("上午时段，可以预订午餐");
                    break;
                case "中午":
                    sb.append("午餐时间，建议及时下单");
                    break;
                case "下午":
                    sb.append("下午时段，可以预订晚餐");
                    break;
                case "晚上":
                    sb.append("晚餐时间，祝您用餐愉快");
                    break;
                case "深夜":
                    sb.append("夜深了，请注意饮食健康");
                    break;
            }

            log.info("✅ [Tool] 获取当前时间成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取当前时间失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 判断当前时间段
     *
     * @return 时间段
     */
    @Tool("""
        判断当前属于哪个时间段

        **时间段定义：**
        - 早晨：5:00-8:00（早餐时间）
        - 上午：8:00-11:00（工作时间）
        - 中午：11:00-13:00（午餐时间）
        - 下午：13:00-17:00（工作时间）
        - 晚上：17:00-20:00（晚餐时间）
        - 深夜：20:00-5:00（休息时间）

        **何时使用：**
        - 推荐时段菜品
        - 问候语
        - 判断营业状态

        **返回：** 时间段名称和建议
        """)
    public String getTimePeriod() {
        log.info("🔍 [Tool] 获取时间段");

        try {
            LocalTime now = LocalTime.now();
            String period = getTimePeriod(now);
            String greeting = getGreeting(period);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🕐 **%s**\n\n", period));
            sb.append(String.format("%s\n\n", greeting));

            // 添加时段建议
            sb.append("💡 **时段建议**\n");
            switch (period) {
                case "早晨" -> {
                    sb.append("  • 早餐推荐：包子、豆浆、鸡蛋\n");
                    sb.append("  • 营养重点：蛋白质和维生素\n");
                    break;
                }
                case "上午" -> {
                    sb.append("  • 可以预订午餐\n");
                    sb.append("  • 建议提前下单避免高峰\n");
                    break;
                }
                case "中午" -> {
                    sb.append("  • 午餐时间，建议及时下单\n");
                    sb.append("  • 推荐均衡搭配：主食+菜品+汤\n");
                    break;
                }
                case "下午" -> {
                    sb.append("  • 可以预订晚餐\n");
                    sb.append("  • 下午茶时间，可以加点心\n");
                    break;
                }
                case "晚上" -> {
                    sb.append("  • 晚餐时间，建议清淡饮食\n");
                    sb.append("  • 避免过于油腻，影响睡眠\n");
                    break;
                }
                case "深夜" -> {
                    sb.append("  • 夜深了，建议少吃\n");
                    sb.append("  • 如果需要，选择易消化的食物\n");
                    break;
                }
            }

            log.info("✅ [Tool] 获取时间段成功: {}", period);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取时间段失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 判断商家是否营业
     *
     * @param businessHours 营业时间（格式：HH:mm-HH:mm）
     * @return 营业状态
     */
    @Tool("""
        判断商家当前是否营业

        **何时使用：**
        - 下单前确认
        - 查询营业状态

        **参数：** businessHours - 营业时间（如：09:00-21:00）

        **返回：** 营业状态
        """)
    public String isOpenNow(
        @P("营业时间，格式：HH:mm-HH:mm，如：09:00-21:00") String businessHours
    ) {
        log.info("🔍 [Tool] 检查营业状态，businessHours: {}", businessHours);

        try {
            if (businessHours == null || businessHours.isEmpty()) {
                return "⚠️ 营业时间未设置";
            }

            String[] times = businessHours.split("-");
            if (times.length != 2) {
                return "⚠️ 营业时间格式错误，应为 HH:mm-HH:mm";
            }

            LocalTime now = LocalTime.now();
            LocalTime openTime = LocalTime.parse(times[0]);
            LocalTime closeTime = LocalTime.parse(times[1]);

            boolean isOpen = now.isAfter(openTime) && now.isBefore(closeTime);

            StringBuilder sb = new StringBuilder();
            if (isOpen) {
                sb.append("✅ **营业中**\n\n");
                sb.append(String.format("  • 营业时间：%s\n", businessHours));
                sb.append(String.format("  • 当前时间：%s\n", now.format(DateTimeFormatter.ofPattern("HH:mm"))));
                sb.append("  • 状态：可以正常下单\n");
            } else {
                sb.append("❌ **未营业**\n\n");
                sb.append(String.format("  • 营业时间：%s\n", businessHours));
                sb.append(String.format("  • 当前时间：%s\n", now.format(DateTimeFormatter.ofPattern("HH:mm"))));

                if (now.isBefore(openTime)) {
                    sb.append(String.format("  • 距离开营还有：%d分钟\n",
                        (openTime.toSecondOfDay() - now.toSecondOfDay()) / 60));
                } else {
                    sb.append(String.format("  • 距离打烊已有：%d分钟\n",
                        (now.toSecondOfDay() - closeTime.toSecondOfDay()) / 60));
                }
            }

            log.info("✅ [Tool] 检查营业状态成功，isOpen: {}", isOpen);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 检查营业状态失败", e);
            return "❌ 检查失败：" + e.getMessage();
        }
    }

    /**
     * 估算配送时间
     *
     * @param merchantName 商家名称
     * @param userLocation 用户位置
     * @return 预计送达时间
     */
    @Tool("""
        估算订单的配送时间

        **估算因素：**
        - 商家距离
        - 当前时间段
        - 订单量（假设）

        **何时使用：**
        - 下单前告知送达时间
        - 用户询问配送时间

        **参数：**
        - merchantName - 商家名称
        - userLocation - 用户位置

        **返回：** 预计送达时间（分钟）
        """)
    public String estimateDeliveryTime(
        @P("商家名称") String merchantName,
        @P("用户位置，如：学生宿舍1栋") String userLocation
    ) {
        log.info("🔍 [Tool] 估算配送时间，merchant: {}, location: {}", merchantName, userLocation);

        try {
            LocalTime now = LocalTime.now();
            String period = getTimePeriod(now);

            // 基础配送时间（分钟）
            int baseTime = 20;

            // 根据时段调整
            if (period.equals("中午")) {
                baseTime = 30; // 午餐高峰
            } else if (period.equals("晚上")) {
                baseTime = 35; // 晚餐高峰
            } else if (period.equals("深夜")) {
                baseTime = 15; // 深夜订单少
            }

            // 距离因素（简化）
            if (userLocation != null && userLocation.contains("宿舍")) {
                baseTime += 5; // 宿舍区可能稍远
            }

            // 计算预计送达时间
            LocalTime estimatedTime = now.plusMinutes(baseTime);

            StringBuilder sb = new StringBuilder();
            sb.append("🚚 配送时间估算\n\n");
            sb.append(String.format("  • 商家：%s\n", merchantName));
            sb.append(String.format("  • 配送到：%s\n", userLocation));
            sb.append(String.format("  • 当前时段：%s\n", period));
            sb.append(String.format("  • 预计送达：%d分钟\n\n", baseTime));
            sb.append(String.format("⏰ 预计到达时间：%s",
                estimatedTime.format(DateTimeFormatter.ofPattern("HH:mm"))));

            // 添加建议
            sb.append("\n\n💡 **提示**");
            if (period.equals("中午") || period.equals("晚上")) {
                sb.append("\n  当前为用餐高峰，建议提前下单");
            } else {
                sb.append("\n  当前时段订单较少，配送较快");
            }

            log.info("✅ [Tool] 估算配送时间成功，时间: {}分钟", baseTime);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 估算配送时间失败", e);
            return "❌ 估算失败：" + e.getMessage();
        }
    }

    /**
     * 获取今日营业状态
     *
     * @return 今日营业信息
     */
    @Tool("""
        获取今日的营业时间信息

        **信息包含：**
        - 当前日期和星期
        - 当前时段
        - 是否工作日

        **何时使用：**
        - 判断营业情况
        - 推荐时段菜品

        **返回：** 今日营业信息
        """)
    public String getTodayInfo() {
        log.info("🔍 [Tool] 获取今日信息");

        try {
            LocalDate today = LocalDate.now();
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            LocalTime now = LocalTime.now();
            String period = getTimePeriod(now);

            StringBuilder sb = new StringBuilder();
            sb.append("📅 今日信息\n\n");
            sb.append(String.format("  • 日期：%s\n",
                today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))));
            sb.append(String.format("  • 星期：%s\n", formatDayOfWeek(dayOfWeek)));
            sb.append(String.format("  • 时段：%s\n", period));

            // 判断是否工作日
            boolean isWeekday = dayOfWeek.getValue() <= 5;
            sb.append(String.format("  • 类型：%s\n\n", isWeekday ? "工作日" : "周末"));

            // 营业建议
            sb.append("💡 **营业提示**\n");
            if (isWeekday) {
                sb.append("  • 工作日午餐和晚餐高峰期订单较多\n");
                sb.append("  • 建议提前10-15分钟下单");
            } else {
                sb.append("  • 周末用餐时间较晚\n");
                sb.append("  • 高峰期可能持续到13:30和19:30");
            }

            log.info("✅ [Tool] 获取今日信息成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取今日信息失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 获取时间段名称
     */
    private String getTimePeriod(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 8) {
            return "早晨";
        } else if (hour >= 8 && hour < 11) {
            return "上午";
        } else if (hour >= 11 && hour < 13) {
            return "中午";
        } else if (hour >= 13 && hour < 17) {
            return "下午";
        } else if (hour >= 17 && hour < 20) {
            return "晚上";
        } else {
            return "深夜";
        }
    }

    /**
     * 获取问候语
     */
    private String getGreeting(String period) {
        switch (period) {
            case "早晨":
                return "🌅 早上好！新的一天从营养早餐开始";
            case "上午":
                return "☀️ 上午好！记得预订午餐哦";
            case "中午":
                return "🌞 中午好！午餐时间到了";
            case "下午":
                return "☕ 下午好！来点下午茶吗";
            case "晚上":
                return "🌙 晚上好！享用美味的晚餐吧";
            case "深夜":
                return "🌜 夜深了，注意休息";
            default:
                return "您好！";
        }
    }

    /**
     * 格式化星期
     */
    private String formatDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "星期一";
            case TUESDAY:
                return "星期二";
            case WEDNESDAY:
                return "星期三";
            case THURSDAY:
                return "星期四";
            case FRIDAY:
                return "星期五";
            case SATURDAY:
                return "星期六";
            case SUNDAY:
                return "星期日";
            default:
                return "";
        }
    }
}

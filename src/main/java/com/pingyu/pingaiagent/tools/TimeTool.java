package com.pingyu.pingaiagent.tools;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * 案件 Tool-008: 时间工具
 * <p>
 * 目标: 为 AI 提供时间感知能力 (当前时间、日期计算)。
 * 架构: 自动注册 (implements AgentTool)
 */
@Slf4j
@Component
public class TimeTool implements AgentTool {

    /**
     * 获取当前时间
     *
     * @param timezoneId 时区ID (可选, 如 "Asia/Shanghai", "Europe/London")
     * @return 格式化的时间字符串
     */
    @Tool(description = "Get the current date and time. You can specify a timezone (e.g., 'Asia/Shanghai', 'Europe/London'). If not specified, defaults to the server's timezone.")
    public String getCurrentTime(@ToolParam(description = "The timezone ID (optional)") String timezoneId) {
        try {
            // 1. 确定时区
            ZoneId zone;
            if (StrUtil.isBlank(timezoneId)) {
                zone = ZoneId.systemDefault();
            } else {
                try {
                    zone = ZoneId.of(timezoneId);
                } catch (Exception e) {
                    return "Error: Invalid timezone ID '" + timezoneId + "'. Using default instead.";
                }
            }

            // 2. 获取时间
            ZonedDateTime now = ZonedDateTime.now(zone);

            // 3. 格式化 (SOP 1 决策: 包含星期和时区)
            // 格式示例: 2025-12-05 星期五 17:02:58 (CST)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm:ss (z)", Locale.CHINA);

            String result = now.format(formatter);
            log.info("AI 查询时间: {} -> {}", zone, result);
            return result;

        } catch (Exception e) {
            log.error("查询时间失败", e);
            return "Error getting current time: " + e.getMessage();
        }
    }

    /**
     * 计算日期差值
     *
     * @param targetDate 目标日期 (yyyy-MM-dd)
     * @return 距离天数描述
     */
    @Tool(description = "Calculate the difference in days between today and a target date.")
    public String dateDiff(@ToolParam(description = "The target date (format: yyyy-MM-dd)") String targetDate) {
        try {
            // 1. 解析输入
            Date target = DateUtil.parse(targetDate);
            Date now = DateUtil.date();

            // 2. 计算差值 (使用 Hutool)
            long diffDays = DateUtil.between(now, target, DateUnit.DAY);

            // 3. 判断方向 (未来还是过去)
            if (target.after(now)) {
                return "距离 " + targetDate + " 还有 " + diffDays + " 天。";
            } else {
                return "距离 " + targetDate + " 已过去 " + diffDays + " 天。";
            }

        } catch (Exception e) {
            log.error("日期计算失败: {}", targetDate, e);
            return "Error parsing date. Please use format yyyy-MM-dd.";
        }
    }
}
package top.fblue.watermelon.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 */
public class DateTimeUtil {
    
    /**
     * 默认日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 格式化LocalDateTime为字符串
     * 
     * @param dateTime LocalDateTime对象
     * @return 格式化后的字符串，如果为null则返回null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * 格式化LocalDateTime为字符串（自定义格式）
     * 
     * @param dateTime LocalDateTime对象
     * @param pattern 格式模式
     * @return 格式化后的字符串，如果为null则返回null
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
    
    /**
     * 解析字符串为LocalDateTime
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象，如果解析失败则返回null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtil.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
} 
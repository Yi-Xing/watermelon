package top.fblue.watermelon.common.utils;

/**
 * 字符串工具类
 */
public class StringUtil {
    
    /**
     * 获取非空字符串，如果为null则返回空字符串
     */
    public static String getNonEmptyString(String value) {
        return value != null ? value : "";
    }
    
    /**
     * 获取非空字符串，如果为null则返回默认值
     */
    public static String getNonEmptyString(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
    
    /**
     * 判断字符串是否为空或null
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
}

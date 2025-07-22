package top.fblue.watermelon.common.utils;


/**
 * 字符串处理工具
 *
 * @author wangx
 */
public class StringUtil {

    /**
     * 消除字符串首尾空格，以及不可见字符
     */
    public static String stringTrim(String str) {
        return str.trim().replaceAll("\\p{C}", "");
    }
}

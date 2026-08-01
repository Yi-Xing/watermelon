package top.fblue.watermelon;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.common.utils.EncryptionUtil;

import java.security.MessageDigest;

/**
 * 用于项目初始化密码
 */
public class PasswordTest {

    /**
     * 用于初始化时，使用jwt生成密码
     */
    @Test
    void contextLoads() {
        String password = "admin123";
        String passwordMD5 = md5(password);
        System.out.println("md5:"+passwordMD5);
        System.out.println("password:"+EncryptionUtil.encode(passwordMD5,2L));
    }

    /**
     * 计算初始化密码的 MD5 十六进制摘要。
     *
     * @param password 原始密码
     * @return 小写十六进制 MD5 摘要
     */
    private static String md5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes("UTF-8"));

            // 转成十六进制
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 error", e);
        }
    }
}

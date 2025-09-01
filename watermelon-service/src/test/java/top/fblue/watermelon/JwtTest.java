package top.fblue.watermelon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.fblue.watermelon.common.utils.EncryptionUtil;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * 用于生成JWT密钥
 */
@SpringBootTest
public class JwtTest {

    /**
     * 生成 jwt 秘钥
     */
    @Test
    void generateJwtSecret() {
        System.out.println("=== JWT密钥生成工具 ===");
        System.out.println();

        try {
            // 生成不同长度的密钥
            generateKeys();

            System.out.println(7 * 24 * 60 * 60 * 1000L);
            System.out.println("=== 使用说明 ===");
            System.out.println("1. 将生成的密钥复制到application.properties中");
            System.out.println("2. 格式：jwt.secret=你的密钥");
            System.out.println("3. 生产环境建议使用256位或512位密钥");
            System.out.println("4. 定期更换密钥以提高安全性");

        } catch (Exception e) {
            System.err.println("生成密钥时发生错误: " + e.getMessage());
        }
    }

    /**
     * 生成不同长度的JWT密钥
     */
    private static void generateKeys() {
        // 生成256位密钥（32字节）
        byte[] key256 = generateRandomKey(32);
        System.out.println("256位密钥 (Base64编码):");
        System.out.println(Base64.getEncoder().encodeToString(key256));
        System.out.println();

        // 生成512位密钥（64字节）
        byte[] key512 = generateRandomKey(64);
        System.out.println("512位密钥 (Base64编码):");
        System.out.println(Base64.getEncoder().encodeToString(key512));
        System.out.println();
    }

    /**
     * 生成指定长度的随机密钥
     */
    private static byte[] generateRandomKey(int length) {
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] key = new byte[length];
        random.nextBytes(key);
        return key;
    }
}

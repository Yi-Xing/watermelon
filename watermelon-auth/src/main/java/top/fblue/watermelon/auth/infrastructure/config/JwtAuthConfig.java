package top.fblue.watermelon.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 验证配置（与用户中心签发 JWT 时保持一致）
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtAuthConfig {

    /**
     * JWT 签名密钥（需与用户中心保持一致）
     */
    private String secret;

    /**
     * JWT 签发者（用于校验 iss）
     */
    private String issuer;
}

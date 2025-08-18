package top.fblue.watermelon.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * 用于配置JWT相关的参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    /**
     * 是否启用 jwt
     */
    private Boolean enable;
    
    /**
     * JWT密钥
     */
    private String secret;
    
    /**
     * JWT过期时间（毫秒）
     * 默认7天
     */
    private long expiration;

    /**
     * JWT签发者
     */
    private String issuer;
}

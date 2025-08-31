package top.fblue.watermelon.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "system")
public class SystemConfig {
    /**
     * 系统 环境
     */
    private String env;
    /**
     * 系统 code
     */
    private String code;
}

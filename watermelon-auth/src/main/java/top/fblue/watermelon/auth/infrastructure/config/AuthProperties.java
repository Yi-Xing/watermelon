package top.fblue.watermelon.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auth 模块配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 用户中心登录地址，token 验证失败时跳转
     * 例如：http://user-center.example.com/login
     */
    private String loginUrl;

    /**
     * 回调地址域名白名单（允许的 return_url 域名列表）
     * 例如：system-a.example.com, system-b.example.com
     */
    private List<String> callbackDomainWhitelist = new ArrayList<>();

    /**
     * 其他系统的 Dubbo RPC 地址
     * key: 系统标识（systemCode）, value: dubbo url
     * 例如：system-a -> dubbo://system-a.example.com:20880
     */
    private Map<String, String> otherSystemUrls = new HashMap<>();

    /**
     * 授权码（code）的过期时间（秒），默认 5 分钟
     */
    private long codeTtlSeconds = 300L;
}

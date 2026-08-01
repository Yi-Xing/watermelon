package top.fblue.watermelon.auth.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auth 模块配置
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 允许接入用户中心的 SSO Client 配置，key 为 clientId。
     */
    private Map<String, @Valid Client> clients = new HashMap<>();

    /** 用户中心自身在全局会话中的 Client 标识。 */
    @NotBlank
    private String serverClientId = "watermelon";

    /**
     * 授权码（code）的过期时间（秒），默认 5 分钟
     */
    @Min(1)
    private long codeTtlSeconds = 300L;

    /** 全局 SSO 会话有效期，单位秒，默认 30 天。 */
    @Min(1)
    private long sessionTtlSeconds = 2_592_000L;

    /** 是否允许注册 HTTP 回调地址；仅建议在本地开发环境启用。 */
    private boolean allowInsecureRedirects;

    /**
     * 单个 SSO 客户端的授权与退出通知配置。
     */
    @Data
    public static class Client {
        /** 是否允许该 Client 发起 SSO 授权和 code 兑换。 */
        private boolean enabled = true;

        /** 允许的固定后端回调地址白名单。 */
        private List<String> redirectUris = new ArrayList<>();

        /** 用户中心发送 Back-Channel sid 撤销通知时使用的 Dubbo 直连地址。 */
        private String dubboUrl;

        /** 启用的 Client 必须同时配置回调白名单和退出通知地址。 */
        @AssertTrue(message = "启用的 SSO Client 必须配置 redirectUris 和 dubboUrl")
        public boolean isEnabledConfigurationValid() {
            return !enabled || (redirectUris != null
                    && !redirectUris.isEmpty()
                    && redirectUris.stream().allMatch(StringUtils::hasText)
                    && StringUtils.hasText(dubboUrl));
        }
    }
}

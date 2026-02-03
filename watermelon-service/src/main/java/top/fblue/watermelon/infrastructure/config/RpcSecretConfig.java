package top.fblue.watermelon.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.fblue.watermelon.dubbo.filter.RpcSecretHolder;

/**
 * RPC 秘钥鉴权配置
 * 用于 Dubbo 服务端校验调用方传递的秘钥
 */
@Configuration
@ConfigurationProperties(prefix = "rpc")
public class RpcSecretConfig {

    /**
     * 服务端与调用方约定的秘钥，调用方需在 RpcContext 中传递相同值
     */
    private String secret = "";

    /**
     * 是否启用 RPC 秘钥鉴权
     */
    private boolean authEnabled = true;

    @PostConstruct
    public void init() {
        RpcSecretHolder.setSecret(secret);
        RpcSecretHolder.setAuthEnabled(authEnabled);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }
}

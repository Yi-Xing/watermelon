package top.fblue.watermelon.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RPC 秘钥鉴权配置
 * 用于 Dubbo 服务端校验调用方传递的秘钥。
 * 启动后通过 getInstance() 可获取本单例，供 SPI 加载的 Filter 等非 Spring 组件读取配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rpc")
public class RpcSecretConfig {

    private static volatile RpcSecretConfig instance;

    /**
     * 服务端与调用方约定的秘钥，调用方需在 RpcContext 中传递相同值
     */
    private String secret = "";

    /**
     * 是否启用 RPC 秘钥鉴权
     */
    private boolean authEnabled = true;

    @PostConstruct
    void registerInstance() {
        instance = this;
    }

    /** 供 SPI 加载的 Filter 等获取 Spring 管理的配置单例 */
    public static RpcSecretConfig getInstance() {
        return instance;
    }

}

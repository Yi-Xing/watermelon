package top.fblue.watermelon.auth.infrastructure.repository;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.api.SystemTokenRevokeRpc;
import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.auth.domain.user.repository.AuthRepository;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过 Dubbo 直连方式向各 SSO 客户端发送全局会话撤销通知的仓储实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepository {

    /** SSO 客户端及其 Dubbo 地址配置。 */
    private final AuthProperties authProperties;
    /** 按客户端标识缓存的 Dubbo 引用和代理。 */
    private final Map<String, ClientReference> clients = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySystemRevokeSession(String clientId, TokenRevokeRequest request) {
        AuthProperties.Client client = authProperties.getClients().get(clientId);
        if (client == null || !StringUtils.hasText(client.getDubboUrl())) {
            log.warn("SSO Client {} 未配置 Dubbo URL，跳过 sid 撤销通知", clientId);
            return;
        }
        try {
            clients.computeIfAbsent(clientId, ignored -> buildSystemRpc(client.getDubboUrl()))
                    .proxy().revokeSession(request);
            log.info("已通知 SSO Client {} 撤销 sidPrefix={}", clientId, sidPrefix(request.getSid()));
        } catch (Exception e) {
            // 按当前实施范围，仅同步通知并记录错误；可靠重试队列后续实现。
            log.error("通知 SSO Client {} 撤销 sidPrefix={} 失败",
                    clientId, sidPrefix(request.getSid()), e);
        }
    }

    /**
     * 应用关闭时销毁动态创建的 Dubbo 引用并清空缓存。
     */
    @PreDestroy
    void destroyReferences() {
        clients.values().forEach(client -> client.reference().destroy());
        clients.clear();
    }

    /**
     * 根据客户端直连地址创建退出通知 RPC 引用。
     *
     * @param dubboUrl 客户端 Dubbo 直连地址
     * @return 同时包含引用配置和服务代理的对象
     */
    private ClientReference buildSystemRpc(String dubboUrl) {
        ReferenceConfig<SystemTokenRevokeRpc> reference = new ReferenceConfig<>();
        reference.setInterface(SystemTokenRevokeRpc.class);
        reference.setUrl(dubboUrl);
        reference.setTimeout(3000);
        reference.setRetries(0);
        return new ClientReference(reference, reference.get());
    }

    /**
     * 截取 SID 前缀用于日志定位，避免输出完整会话标识。
     *
     * @param sid 全局会话标识
     * @return 最多八个字符的 SID 前缀
     */
    private String sidPrefix(String sid) {
        if (!StringUtils.hasText(sid)) {
            return "";
        }
        return sid.substring(0, Math.min(8, sid.length()));
    }

    /**
     * 同时保存 Dubbo 引用配置和代理，便于应用关闭时释放网络资源。
     *
     * @param reference Dubbo 引用配置
     * @param proxy 退出通知服务代理
     */
    private record ClientReference(ReferenceConfig<SystemTokenRevokeRpc> reference,
                                   SystemTokenRevokeRpc proxy) {
    }
}

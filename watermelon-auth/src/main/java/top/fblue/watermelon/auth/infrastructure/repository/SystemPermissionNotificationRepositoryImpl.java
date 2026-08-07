package top.fblue.watermelon.auth.infrastructure.repository;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.api.SystemPermissionChangeRpc;
import top.fblue.watermelon.api.request.PermissionChangeRequest;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionNotificationRepository;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;
import top.fblue.watermelon.auth.infrastructure.converter.PermissionChangeRequestConverter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过 Dubbo 直连向业务系统发送权限缓存失效通知。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SystemPermissionNotificationRepositoryImpl implements PermissionNotificationRepository {

    /** 用户中心认证及业务系统客户端配置。 */
    private final AuthProperties authProperties;

    /** 权限变更通知请求转换器。 */
    private final PermissionChangeRequestConverter requestConverter;

    /** 按业务系统缓存的动态 Dubbo 引用。 */
    private final Map<String, ClientReference> clients = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findNotifiableSystemCodes() {
        return authProperties.getClients().entrySet().stream()
                .filter(entry -> isNotifiable(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUserChanged(String systemCode,
                                  String eventId,
                                  Long userId,
                                  long userPermissionVersion,
                                  long systemPermissionVersion) {
        PermissionChangeRequest request = requestConverter.toUserChangedRequest(
                eventId, systemCode, userId, userPermissionVersion, systemPermissionVersion);
        notifyClient(systemCode, requiredClient(systemCode), request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySystemChanged(String systemCode,
                                    String eventId,
                                    long systemPermissionVersion) {
        PermissionChangeRequest request = requestConverter.toSystemChangedRequest(
                eventId, systemCode, systemPermissionVersion);
        notifyClient(systemCode, requiredClient(systemCode), request);
    }

    /**
     * 应用关闭时销毁动态创建的 Dubbo 引用。
     */
    @PreDestroy
    void destroyReferences() {
        clients.values().forEach(client -> {
            try {
                client.reference().destroy();
            } catch (IllegalStateException exception) {
                // Dubbo 的关闭钩子可能早于 Spring Bean 销毁执行，此时引用已随框架释放。
                log.debug("Dubbo 框架已关闭，无需重复销毁权限通知引用", exception);
            }
        });
        clients.clear();
    }

    /**
     * 判断客户端是否启用并配置了通知地址。
     *
     * @param client 客户端配置
     * @return 是否可以发送权限变更通知
     */
    private boolean isNotifiable(AuthProperties.Client client) {
        return client != null && client.isEnabled() && StringUtils.hasText(client.getDubboUrl());
    }

    /**
     * 向指定客户端发送权限缓存失效通知。
     *
     * @param systemCode 目标系统编码
     * @param client 客户端配置
     * @param request 权限变更请求
     */
    private void notifyClient(String systemCode, AuthProperties.Client client, PermissionChangeRequest request) {
        clients.computeIfAbsent(systemCode, ignored -> buildSystemRpc(client.getDubboUrl()))
                .proxy().permissionChanged(request);
        log.info("已通知系统 {} 失效权限缓存，userId={}，systemPermissionVersion={}，userPermissionVersion={}",
                systemCode, request.getUserId(), request.getSystemPermissionVersion(),
                request.getUserPermissionVersion());
    }

    /**
     * 获取目标系统通知配置。
     *
     * @param systemCode 目标系统编码
     * @return 已启用的目标系统配置
     */
    private AuthProperties.Client requiredClient(String systemCode) {
        AuthProperties.Client client = authProperties.getClients().get(systemCode);
        if (!isNotifiable(client)) {
            throw new IllegalStateException("系统未启用或未配置权限通知地址：" + systemCode);
        }
        return client;
    }

    /**
     * 根据直连地址创建权限变更通知 RPC 引用。
     *
     * @param dubboUrl Dubbo 直连地址
     * @return RPC 引用与代理
     */
    private ClientReference buildSystemRpc(String dubboUrl) {
        ReferenceConfig<SystemPermissionChangeRpc> reference = new ReferenceConfig<>();
        reference.setInterface(SystemPermissionChangeRpc.class);
        reference.setUrl(dubboUrl);
        reference.setTimeout(3000);
        reference.setRetries(0);
        return new ClientReference(reference, reference.get());
    }

    /**
     * 保存 Dubbo 引用配置和服务代理。
     *
     * @param reference Dubbo 引用配置
     * @param proxy 权限变更通知代理
     */
    private record ClientReference(ReferenceConfig<SystemPermissionChangeRpc> reference,
                                   SystemPermissionChangeRpc proxy) {
    }
}

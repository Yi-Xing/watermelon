package top.fblue.watermelon.auth.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.SystemTokenRevokeRpc;
import top.fblue.watermelon.api.UserRpc;
import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.api.response.UserBaseResponse;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.repository.AuthRepository;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;
import top.fblue.watermelon.auth.infrastructure.converter.AuthRpcConverter;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepository {

    @DubboReference(url = "${dubbo.watermelon.url}")
    private UserRpc userRpc;

    private final AuthRpcConverter authRpcConverter;
    private final AuthProperties authProperties;

    @Override
    public User findUserById(Long userId) {
        try {
            ApiResponse<UserBaseResponse> response = userRpc.getUser(userId);
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.warn("查询用户信息失败，userId: {}, response: {}", userId, response);
                return null;
            }
            return authRpcConverter.toDomain(response.getData());
        } catch (Exception e) {
            log.error("调用 UserRpc.getUser 异常，userId: {}", userId, e);
            return null;
        }
    }

    @Override
    public void notifySystemRevokeToken(String systemCode, TokenRevokeRequest request) {
        String dubboUrl = authProperties.getOtherSystemUrls().get(systemCode);
        if (!StringUtils.hasText(dubboUrl)) {
            log.warn("系统 {} 未配置 Dubbo URL，跳过通知 jti: {}", systemCode, request.getJti());
            return;
        }
        try {
            SystemTokenRevokeRpc rpc = buildSystemRpc(dubboUrl);
            rpc.revokeToken(request);
            log.info("已通知系统 {} 吊销 jti: {}", systemCode, request.getJti());
        } catch (Exception e) {
            // 其他系统通知失败，只打印 error 日志，不影响主流程
            log.error("通知系统 {} 吊销 jti 失败，jti: {}, error: {}", systemCode, request.getJti(), e.getMessage(), e);
        }
    }

    /**
     * 动态创建其他系统的 Dubbo Reference
     */
    private SystemTokenRevokeRpc buildSystemRpc(String dubboUrl) {
        ReferenceConfig<SystemTokenRevokeRpc> reference = new ReferenceConfig<>();
        reference.setInterface(SystemTokenRevokeRpc.class);
        reference.setUrl(dubboUrl);
        reference.setTimeout(3000);
        reference.setRetries(0);
        return reference.get();
    }
}

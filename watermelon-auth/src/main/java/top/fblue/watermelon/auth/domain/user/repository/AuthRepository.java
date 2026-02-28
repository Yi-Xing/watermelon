package top.fblue.watermelon.auth.domain.user.repository;

import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.auth.domain.user.entity.User;

/**
 * Auth 领域仓储接口
 */
public interface AuthRepository {

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，不存在返回 null
     */
    User findUserById(Long userId);

    /**
     * 通知指定系统吊销 jti（调用该系统的 SystemTokenRevokeRpc）
     * 失败时只打印 error 日志，不抛出异常
     *
     * @param systemCode 系统标识（对应 auth.other-system-urls 中的 key）
     * @param request    jti、expiresIn、deviceCode
     */
    void notifySystemRevokeToken(String systemCode, TokenRevokeRequest request);
}

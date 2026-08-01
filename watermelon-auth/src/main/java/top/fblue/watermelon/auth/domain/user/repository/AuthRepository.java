package top.fblue.watermelon.auth.domain.user.repository;

import top.fblue.watermelon.api.request.TokenRevokeRequest;

/**
 * Auth 领域仓储接口
 */
public interface AuthRepository {

    /**
     * 通知指定 Client 撤销 sid。当前阶段失败只记录日志，可靠重试按要求暂不实现。
     */
    void notifySystemRevokeSession(String systemCode, TokenRevokeRequest request);
}

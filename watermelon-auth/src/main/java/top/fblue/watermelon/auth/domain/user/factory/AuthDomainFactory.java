package top.fblue.watermelon.auth.domain.user.factory;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;

/**
 * SSO 认证领域对象工厂。
 */
@Component
public class AuthDomainFactory {

    /**
     * 创建全局 SSO 会话领域对象。
     *
     * @param sid 全局会话唯一标识
     * @param userId 会话所属用户 ID
     * @param deviceCode 登录设备标识
     * @param expireAtEpochSeconds 会话过期时间，Unix 秒级时间戳
     * @return 全局 SSO 会话
     */
    public SsoSessionInfo createSession(String sid, Long userId, String deviceCode,
                                        long expireAtEpochSeconds) {
        return SsoSessionInfo.builder()
                .sid(sid)
                .userId(userId)
                .deviceCode(deviceCode)
                .expireAtEpochSeconds(expireAtEpochSeconds)
                .build();
    }

    /**
     * 创建发送给业务系统的会话撤销通知。
     *
     * @param eventId 退出事件幂等标识
     * @param sid 全局会话唯一标识
     * @param sessionExpireAt 会话过期时间，Unix 秒级时间戳
     * @param reason 退出原因
     * @return 会话撤销通知
     */
    public TokenRevokeRequest createTokenRevokeRequest(String eventId, String sid,
                                                        long sessionExpireAt, String reason) {
        return TokenRevokeRequest.builder()
                .eventId(eventId)
                .sid(sid)
                .sessionExpireAt(sessionExpireAt)
                .reason(reason)
                .build();
    }
}

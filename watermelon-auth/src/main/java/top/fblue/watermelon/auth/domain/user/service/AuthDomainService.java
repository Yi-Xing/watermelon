package top.fblue.watermelon.auth.domain.user.service;

import top.fblue.auth.context.SsoPrincipal;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;

import java.util.Set;

/**
 * SSO 会话与一次性授权码领域服务。
 */
public interface AuthDomainService {

    /**
     * 为用户创建全局 SSO 会话。
     *
     * @param userId 用户 ID
     * @param deviceCode 登录设备标识
     * @return 新建的全局会话
     */
    SsoSessionInfo createSession(Long userId, String deviceCode);

    /**
     * 获取并校验仍然有效且未撤销的全局会话。
     *
     * @param sid 全局会话标识
     * @return 有效的全局会话
     */
    SsoSessionInfo requireActiveSession(String sid);

    /**
     * 查询全局会话，不执行有效期及撤销状态校验。
     *
     * @param sid 全局会话标识
     * @return 会话信息；不存在时返回 {@code null}
     */
    SsoSessionInfo getSession(String sid);

    /**
     * 解析并校验访问令牌，同时确认 SID 和 JTI 未被撤销。
     *
     * @param token 待校验的 JWT 访问令牌
     * @return 校验通过的 SSO 用户身份
     */
    SsoPrincipal validateAccessToken(String token);

    /**
     * 为指定用户、会话和客户端签发一次性授权码。
     *
     * @param userId 用户 ID
     * @param sid 全局会话标识
     * @param clientId SSO 客户端标识
     * @param redirectUri 已校验的客户端回调地址
     * @param sessionExpireAt 全局会话过期时间，Unix 秒级时间戳
     * @return 一次性授权码
     */
    String generateCode(Long userId, String sid, String clientId, String redirectUri, long sessionExpireAt);

    /**
     * 原子消费一次性授权码，并校验客户端及回调地址。
     *
     * @param code 一次性授权码
     * @param clientId SSO 客户端标识
     * @param redirectUri 客户端回调地址
     * @return 授权码关联信息
     */
    AuthCodeInfo consumeCode(String code, String clientId, String redirectUri);

    /**
     * 加载授权流程所需的用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息；用户不存在时返回 {@code null}
     */
    User getUserById(Long userId);

    /**
     * 获取已绑定指定全局会话的客户端。
     *
     * @param sid 全局会话标识
     * @return 已绑定客户端集合
     */
    Set<String> getSessionClients(String sid);

    /**
     * 撤销全局会话，并通知已绑定的客户端撤销本地会话。
     *
     * @param sid 全局会话标识
     * @param eventId 退出事件幂等标识
     * @param reason 退出原因
     */
    void revokeSession(String sid, String eventId, String reason);
}

package top.fblue.watermelon.auth.domain.user.repository;

import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;

import java.util.Set;

/**
 * Auth Redis 操作仓储接口
 */
public interface AuthRedisRepository {

    /**
     * 保存全局 SSO 会话并设置有效期。
     *
     * @param session 全局会话信息
     * @param ttlSeconds Redis 有效期，单位秒
     */
    void saveSession(SsoSessionInfo session, long ttlSeconds);

    /**
     * 根据 SID 查询全局 SSO 会话。
     *
     * @param sid 全局会话标识
     * @return 会话信息；不存在时返回 {@code null}
     */
    SsoSessionInfo findSession(String sid);

    // ==================== code 授权码 ====================

    /**
     * 存储授权码映射：code -> {userId, sid, clientId, redirectUri, sessionExpireAt}。
     *
     * @param code 一次性授权码
     * @param codeInfo 授权码关联信息
     * @param ttlSeconds Redis 有效期，单位秒
     * @return 是否成功写入；授权码已存在时返回 {@code false}
     */
    boolean saveCode(String code, AuthCodeInfo codeInfo, long ttlSeconds);

    /**
     * 根据 code 获取授权信息，并原子性删除（使用一次后即失效）
     *
     * @param code 授权码
     * @return 授权信息，不存在则返回 null
     */
    AuthCodeInfo getAndDeleteCode(String code);

    /**
     * 记录已使用指定全局会话登录的客户端。
     *
     * @param sid 全局会话标识
     * @param clientId SSO 客户端标识
     * @param ttlSeconds 客户端集合有效期，单位秒
     */
    void addSidClient(String sid, String clientId, long ttlSeconds);

    /**
     * 获取指定全局会话已登录的所有客户端。
     *
     * @param sid 全局会话标识
     * @return 已绑定客户端集合
     */
    Set<String> getSidClients(String sid);
}

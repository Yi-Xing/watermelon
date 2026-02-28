package top.fblue.watermelon.auth.domain.user.repository;

import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;

import java.util.Set;

/**
 * Auth Redis 操作仓储接口
 */
public interface AuthRedisRepository {

    // ==================== jti 黑名单 ====================

    /**
     * 检查 jti 是否在黑名单中（已吊销）
     *
     * @param jti JWT 唯一标识
     * @return true 表示已吊销
     */
    boolean isJtiRevoked(String jti);

    /**
     * 将 jti 加入黑名单
     *
     * @param jti        JWT 唯一标识
     * @param deviceCode 设备 code（作为 value 存储）
     * @param ttlSeconds 过期时间（秒）
     */
    void revokeJti(String jti, String deviceCode, long ttlSeconds);

    // ==================== code 授权码 ====================

    /**
     * 存储授权码映射：code -> {userId, jti, expiresIn}
     *
     * @param code       授权码
     * @param userId     用户ID
     * @param jti        JWT 唯一标识
     * @param expiresIn  JWT 剩余有效时间（秒）
     * @param ttlSeconds code 的过期时间（秒）
     */
    void saveCode(String code, Long userId, String jti, long expiresIn, long ttlSeconds);

    /**
     * 根据 code 获取授权信息，并原子性删除（使用一次后即失效）
     *
     * @param code 授权码
     * @return 授权信息，不存在则返回 null
     */
    AuthCodeInfo getAndDeleteCode(String code);

    // ==================== jti -> 已登录系统 ====================

    /**
     * 记录该 jti 对应的系统（用于退出登录时通知各系统）
     *
     * @param jti        JWT 唯一标识
     * @param systemCode 系统标识
     * @param ttlSeconds 过期时间（秒，与 JWT 保持一致）
     */
    void addJtiSystem(String jti, String systemCode, long ttlSeconds);

    /**
     * 获取 jti 关联的所有系统标识
     *
     * @param jti JWT 唯一标识
     * @return 系统标识集合
     */
    Set<String> getJtiSystems(String jti);
}

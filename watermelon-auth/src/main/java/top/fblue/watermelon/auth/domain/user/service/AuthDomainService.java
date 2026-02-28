package top.fblue.watermelon.auth.domain.user.service;

import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;

/**
 * Auth 领域服务
 */
public interface AuthDomainService {

    /**
     * 生成授权码，存入 Redis
     *
     * @param userId    用户ID
     * @param jti       JWT 唯一标识
     * @param expiresIn token 剩余有效时间（秒）
     * @return 生成的授权码
     */
    String generateCode(Long userId, String jti, long expiresIn);

    /**
     * 消费授权码：从 Redis 取出并删除，同时记录 jti -> callerSystem 映射
     *
     * @param code         授权码
     * @param callerSystem 调用方系统标识
     * @return 授权码关联的信息，code 不存在或已过期则返回 null
     */
    AuthCodeInfo consumeCode(String code, String callerSystem);

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 检查 jti 是否已被吊销（在黑名单中）
     *
     * @param jti JWT 唯一标识
     * @return true 表示已吊销
     */
    boolean isJtiRevoked(String jti);

    /**
     * 退出登录：将 jti 加入黑名单，并通知所有关联系统
     *
     * @param jti        JWT 唯一标识
     * @param deviceCode 设备 code
     * @param expiresIn  token 剩余有效时间（秒），用作黑名单 TTL
     */
    void revokeToken(String jti, String deviceCode, long expiresIn);
}

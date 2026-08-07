package top.fblue.watermelon.auth.infrastructure.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionVersionRepository;

import java.util.List;

/**
 * 基于 Redis 的权限版本仓储。
 */
@Repository
public class RedisPermissionVersionRepositoryImpl implements PermissionVersionRepository {

    /**
     * 按权限变更事件幂等地递增权限版本的 Redis Lua 脚本。
     *
     * <p>权限变更记录采用事务发件箱异步处理，同一条记录可能因为通知失败、服务宕机或
     * 多实例并发领取而被重复执行。如果在 Java 中分别执行查询事件、递增版本和保存事件
     * 三个 Redis 命令，多个执行线程可能同时查询到事件不存在，导致同一事件重复递增版本。
     * Redis 会将单次 Lua 脚本作为不可穿插的整体执行，因此这里使用脚本保证检查事件、
     * 递增版本和保存事件结果三个步骤的原子性。</p>
     *
     * <p>脚本按以下 Redis 命令分支执行：</p>
     * <ol>
     *     <li>执行 {@code GET KEYS[2]} 查询事件幂等结果。查询到版本号时直接返回，
     *     不再执行后续命令。</li>
     *     <li>事件尚未处理时执行 {@code INCR KEYS[1]}，递增用户级或系统级权限版本，
     *     并取得递增后的版本号。</li>
     *     <li>执行 {@code SET KEYS[2] version EX ARGV[1]}，将本事件生成的版本号保存为
     *     幂等结果，并按 {@code ARGV[1]} 秒设置过期时间，供后续重试直接复用。</li>
     * </ol>
     * <p>{@code EX} 过期时间只设置在事件幂等结果 {@code KEYS[2]} 上，权限版本
     * {@code KEYS[1]} 不设置过期时间。</p>
     */
    private static final DefaultRedisScript<Long> INCREMENT_ONCE_SCRIPT = new DefaultRedisScript<>("""
            local existing = redis.call('GET', KEYS[2])
            if existing then
                return tonumber(existing)
            end
            local version = redis.call('INCR', KEYS[1])
            redis.call('SET', KEYS[2], version, 'EX', ARGV[1])
            return version
            """, Long.class);

    /** Redis 字符串操作模板。 */
    private final StringRedisTemplate redisTemplate;
    /** 权限版本 Redis Key 前缀。 */
    private final String keyPrefix;
    /** 单次变更事件版本结果保留秒数。 */
    private final long eventVersionTtlSeconds;

    /**
     * 创建 Redis 权限版本仓储。
     *
     * @param redisTemplate Redis 操作模板
     * @param keyPrefix 权限版本 Key 前缀
     * @param eventVersionTtlSeconds 事件版本结果保留秒数
     */
    public RedisPermissionVersionRepositoryImpl(
            StringRedisTemplate redisTemplate,
            @Value("${permission.redis-key-prefix:permission:}") String keyPrefix,
            @Value("${permission.version-event-ttl-seconds:2592000}") long eventVersionTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.eventVersionTtlSeconds = eventVersionTtlSeconds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUserPermissionVersion(Long userId) {
        return getLong(userPermissionKey(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long incrementUserPermissionVersion(String eventId, Long userId) {
        return incrementOnce(
                userPermissionKey(userId), eventVersionKey(eventId, "user:" + userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSystemPermissionVersion(String systemCode) {
        return getLong(systemPermissionKey(systemCode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long incrementSystemPermissionVersion(String eventId, String systemCode) {
        return incrementOnce(
                systemPermissionKey(systemCode), eventVersionKey(eventId, "system:" + systemCode));
    }

    /**
     * 读取 Redis 中的 long 值，Key 不存在时返回 0。
     *
     * @param key Redis Key
     * @return long 值
     */
    private long getLong(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    /**
     * 按事件幂等地递增指定权限版本；同一事件重试时返回首次生成的版本号。
     *
     * @param versionKey 权限版本 Redis Key
     * @param eventVersionKey 事件生成版本 Redis Key
     * @return 本事件对应的权限版本号
     */
    private long incrementOnce(String versionKey, String eventVersionKey) {
        Long value = redisTemplate.execute(
                INCREMENT_ONCE_SCRIPT,
                List.of(versionKey, eventVersionKey),
                String.valueOf(eventVersionTtlSeconds));
        if (value == null) {
            throw new IllegalStateException("权限版本更新失败");
        }
        return value;
    }

    /**
     * 构造事件幂等版本结果 Key。
     *
     * @param eventId 幂等事件 ID
     * @param scope 版本作用域
     * @return 事件版本结果 Key
     */
    private String eventVersionKey(String eventId, String scope) {
        return keyPrefix + "version-event:" + eventId + ":" + scope;
    }

    /**
     * 构造用户权限版本 Redis Key。
     *
     * @param userId 用户 ID
     * @return 用户权限版本 Key
     */
    private String userPermissionKey(Long userId) {
        return keyPrefix + "version:user:" + userId;
    }

    /**
     * 构造系统权限版本 Redis Key。
     *
     * @param systemCode 系统编码
     * @return 系统权限版本 Key
     */
    private String systemPermissionKey(String systemCode) {
        return keyPrefix + "version:system:" + systemCode;
    }
}

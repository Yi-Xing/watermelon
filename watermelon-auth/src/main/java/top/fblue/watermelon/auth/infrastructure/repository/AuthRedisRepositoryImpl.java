package top.fblue.watermelon.auth.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.domain.user.repository.AuthRedisRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Redis 保存 SSO 会话、一次性授权码和会话客户端关系的仓储实现。
 */
@Slf4j
@Repository
public class AuthRedisRepositoryImpl implements AuthRedisRepository {

    /** Redis 字符串及集合操作模板。 */
    private final StringRedisTemplate redisTemplate;
    /** SSO 会话和授权信息的 JSON 序列化器。 */
    private final ObjectMapper objectMapper;
    /** 所有 SSO Redis Key 使用的统一前缀。 */
    private final String keyPrefix;

    /**
     * 创建 SSO Redis 仓储。
     *
     * @param redisTemplate Redis 操作模板
     * @param objectMapper JSON 序列化器
     * @param keyPrefix SSO Redis Key 前缀
     */
    public AuthRedisRepositoryImpl(StringRedisTemplate redisTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${sso.redis-key-prefix:sso:}") String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.keyPrefix = keyPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSession(SsoSessionInfo session, long ttlSeconds) {
        redisTemplate.opsForValue().set(sessionKey(session.getSid()), toJson(session), ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo findSession(String sid) {
        return fromJson(redisTemplate.opsForValue().get(sessionKey(sid)), SsoSessionInfo.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveCode(String code, AuthCodeInfo codeInfo, long ttlSeconds) {
        Boolean saved = redisTemplate.opsForValue().setIfAbsent(
                codeKey(code), toJson(codeInfo), ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthCodeInfo getAndDeleteCode(String code) {
        return fromJson(redisTemplate.opsForValue().getAndDelete(codeKey(code)), AuthCodeInfo.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSidClient(String sid, String clientId, long ttlSeconds) {
        String key = clientsKey(sid);
        redisTemplate.opsForSet().add(key, clientId);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSidClients(String sid) {
        return redisTemplate.opsForSet().members(clientsKey(sid));
    }

    /**
     * 构造全局会话信息的 Redis Key。
     *
     * @param sid 全局会话标识
     * @return 会话 Redis Key
     */
    private String sessionKey(String sid) {
        return keyPrefix + "session:" + sid;
    }

    /**
     * 构造会话已登录客户端集合的 Redis Key。
     *
     * @param sid 全局会话标识
     * @return 会话客户端集合 Redis Key
     */
    private String clientsKey(String sid) {
        return keyPrefix + "session:" + sid + ":clients";
    }

    /**
     * 构造一次性授权码的 Redis Key，避免在 Key 中保存授权码明文。
     *
     * @param code 一次性授权码
     * @return 授权码 Redis Key
     */
    private String codeKey(String code) {
        return keyPrefix + "code:" + sha256(code);
    }

    /**
     * 将对象序列化为 Redis 中保存的 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSO Redis 数据序列化失败", e);
        }
    }

    /**
     * 将 Redis 中的 JSON 字符串反序列化为指定类型。
     *
     * @param json JSON 字符串
     * @param type 目标类型
     * @param <T> 目标对象类型
     * @return 反序列化结果；输入为 {@code null} 时返回 {@code null}
     */
    private <T> T fromJson(String json, Class<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("SSO Redis 数据反序列化失败, type={}", type.getSimpleName(), e);
            throw new IllegalStateException("SSO Redis 数据损坏", e);
        }
    }

    /**
     * 计算字符串的 SHA-256 十六进制摘要。
     *
     * @param value 待摘要字符串
     * @return SHA-256 十六进制摘要
     */
    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}

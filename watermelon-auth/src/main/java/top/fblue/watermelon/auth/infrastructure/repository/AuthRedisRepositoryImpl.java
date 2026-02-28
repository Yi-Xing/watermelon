package top.fblue.watermelon.auth.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.repository.AuthRedisRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Auth Redis 仓储实现
 * <p>
 * Redis Key 规范：
 * - jti 黑名单：  revoked:{jti}          value: deviceCode
 * - 授权码：      auth:code:{code}        value: JSON{userId,jti,expiresIn}
 * - jti-系统映射：auth:jti:{jti}:systems  value: Set<systemCode>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRedisRepositoryImpl implements AuthRedisRepository {

    private static final String KEY_REVOKED = "revoked:";
    private static final String KEY_CODE = "auth:code:";
    private static final String KEY_JTI_SYSTEMS = "auth:jti:";
    private static final String JTI_SYSTEMS_SUFFIX = ":systems";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== jti 黑名单 ====================

    @Override
    public boolean isJtiRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_REVOKED + jti));
    }

    @Override
    public void revokeJti(String jti, String deviceCode, long ttlSeconds) {
        redisTemplate.opsForValue().set(KEY_REVOKED + jti, deviceCode, ttlSeconds, TimeUnit.SECONDS);
        log.info("jti 已加入黑名单: {}, ttl: {}s", jti, ttlSeconds);
    }

    // ==================== code 授权码 ====================

    @Override
    public void saveCode(String code, Long userId, String jti, long expiresIn, long ttlSeconds) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("jti", jti);
        data.put("expiresIn", expiresIn);
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(KEY_CODE + code, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 code 数据失败", e);
        }
    }

    @Override
    public AuthCodeInfo getAndDeleteCode(String code) {
        String key = KEY_CODE + code;
        // getAndDelete 原子操作，防止并发重复使用
        String json = redisTemplate.opsForValue().getAndDelete(key);
        if (json == null) {
            return null;
        }
        try {
            Map<?, ?> data = objectMapper.readValue(json, Map.class);
            Long userId = ((Number) data.get("userId")).longValue();
            String jti = (String) data.get("jti");
            long expiresIn = ((Number) data.get("expiresIn")).longValue();
            return new AuthCodeInfo(userId, jti, expiresIn);
        } catch (JsonProcessingException e) {
            log.error("反序列化 code 数据失败, code: {}", code, e);
            return null;
        }
    }

    // ==================== jti -> 已登录系统 ====================

    @Override
    public void addJtiSystem(String jti, String systemCode, long ttlSeconds) {
        String key = KEY_JTI_SYSTEMS + jti + JTI_SYSTEMS_SUFFIX;
        redisTemplate.opsForSet().add(key, systemCode);
        // 每次写入时刷新 TTL（取最大值策略，保证 key 不会过早消失）
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Set<String> getJtiSystems(String jti) {
        String key = KEY_JTI_SYSTEMS + jti + JTI_SYSTEMS_SUFFIX;
        return redisTemplate.opsForSet().members(key);
    }
}

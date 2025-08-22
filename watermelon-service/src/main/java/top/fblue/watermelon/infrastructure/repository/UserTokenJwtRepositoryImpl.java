package top.fblue.watermelon.infrastructure.repository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;
import top.fblue.watermelon.infrastructure.config.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户Token仓储 JWT 实现类
 * 使用ConcurrentHashMap存储，支持后期迁移到Redis
 */
@Slf4j
public class UserTokenJwtRepositoryImpl implements UserTokenRepository {

    @Resource
    private JwtConfig jwtConfig;

    private final String USER_ID_KEY = "userId";

    /**
     * 创建Token
     */
    @Override
    public String create(Long userId){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        // claims 是一个 Map<String,Object>，可以放你想存储的业务信息
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_KEY, userId);

        return Jwts.builder()
                .claims(claims)
                // sub，表示该 JWT 的主体（Subject），一般存储用户唯一标识（如用户 ID）。
                .subject(String.valueOf(userId))
                // iss，表示签发者（Issuer）。可以是系统名、域名等，比如 "my-app"。
                .issuer(jwtConfig.getIssuer())
                // iat，表示签发时间。
                .issuedAt(now)
                // exp，表示过期时间。
                .expiration(expiryDate)
                // 密钥和签名算法
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                // 其它字段
                // aud 接收方：可选，多客户端场景下验证是否发给自己
                // nbf（生效时间）：只有需要指定 Token 在某时间后才生效才用
                // jti（唯一ID）：防重放攻击时使用，普通场景可以省略
                .compact();
    }

    @Override
    public UserToken findByToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date createdAt = claims.getIssuedAt();
            Date expiresAt = claims.getExpiration();
            Long userId = claims.get(USER_ID_KEY, Long.class);

            if (userId == null) {
               return null;
            }

            // 将Date转换为LocalDateTime
            LocalDateTime createdAtLocal = createdAt != null ?
                    createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            LocalDateTime expiresAtLocal = expiresAt != null ?
                    expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;

            return UserToken.builder()
                    .userId(userId)
                    .token(token)
                    .createdTime(createdAtLocal)
                    .expireTime(expiresAtLocal)
                    .build();

        } catch (Exception e) {
            log.error("获取JWT令牌信息失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteToken(String token) {
        // jwt 是无状态的，服务端不需要删除。
    }

    @Override
    public void deleteExpiredTokens() {
    }

    /**
     * 生成JWT密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从JWT令牌中获取Claims
     *
     * @param token JWT令牌
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
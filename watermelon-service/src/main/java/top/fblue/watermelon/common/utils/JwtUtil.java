package top.fblue.watermelon.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.fblue.watermelon.infrastructure.config.JwtConfig;
import top.fblue.watermelon.common.dto.JwtTokenInfoDTO;
import top.fblue.watermelon.common.exception.JwtException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT的生成、解析、验证等功能
 */
@Slf4j
@Component
public class JwtUtil {

    @Resource
    private JwtConfig jwtConfig;

    private final String USER_ID_KEY = "userId";

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID
     * @return JWT令牌
     */
    public String generateToken(Long userId) {
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


    /**
     * 生成JWT密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(USER_ID_KEY, Long.class);
    }

    /**
     * 从JWT令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 从JWT令牌中获取Claims
     *
     * @param token JWT令牌
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("解析JWT令牌失败: {}", e.getMessage());
            throw new JwtException("INVALID_TOKEN", "无效的JWT令牌", e, 400);
        } catch (Exception e) {
            log.error("解析JWT令牌时发生未知错误: {}", e.getMessage());
            throw new JwtException("INVALID_TOKEN", "JWT令牌解析失败", e, 400);
        }
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证JWT令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("检查JWT令牌过期状态失败: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 检查JWT令牌是否过期，如果过期则抛出异常
     *
     * @param token JWT令牌
     * @throws JwtException 如果令牌已过期
     */
    public void checkTokenExpiration(String token) {
        if (isTokenExpired(token)) {
            throw new JwtException("TOKEN_EXPIRED", "JWT令牌已过期", 401);
        }
    }

    /**
     * 检查JWT令牌是否即将过期（在指定时间内过期）
     *
     * @param token JWT令牌
     * @param timeBeforeExpiration 过期前的时间（毫秒）
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, long timeBeforeExpiration) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            Date warningTime = new Date(expiration.getTime() - timeBeforeExpiration);
            return now.after(warningTime);
        } catch (Exception e) {
            log.error("检查JWT令牌即将过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 刷新JWT令牌
     *
     * @param token JWT令牌
     * @return 新的JWT令牌
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Long userId = claims.get(USER_ID_KEY, Long.class);
            
            if (userId == null) {
                throw new JwtException("INVALID_TOKEN", "JWT令牌中用户ID无效", 400);
            }

            // 生成新的令牌
            return generateToken(userId);
        } catch (JwtException e) {
            // 重新抛出自定义异常
            throw e;
        } catch (Exception e) {
            log.error("刷新JWT令牌失败: {}", e.getMessage());
            throw new JwtException("INVALID_TOKEN", "无法刷新JWT令牌", e, 400);
        }
    }

    /**
     * 从Authorization头中提取并验证JWT令牌
     *
     * @param authHeader Authorization头
     * @return 用户ID，如果令牌无效则返回null
     */
    public Long extractAndValidateUserId(String authHeader) {
        try {
            String token = TokenUtil.extractTokenFromHeader(authHeader);
            if (token == null) {
                return null;
            }
            
            if (!validateToken(token)) {
                return null;
            }
            
            if (isTokenExpired(token)) {
                return null;
            }
            
            return getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("从Authorization头中提取用户ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取JWT令牌的详细信息
     *
     * @param token JWT令牌
     * @return JWT令牌信息DTO
     */
    public JwtTokenInfoDTO getTokenInfo(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date createdAt = claims.getIssuedAt();
            Date expiresAt = claims.getExpiration();
            Long userId = claims.get(USER_ID_KEY, Long.class);
            
            if (userId == null) {
                throw new JwtException("INVALID_TOKEN", "JWT令牌中用户ID无效", 400);
            }
            
            // 将Date转换为LocalDateTime
            LocalDateTime createdAtLocal = createdAt != null ? 
                createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            LocalDateTime expiresAtLocal = expiresAt != null ? 
                expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            
            return JwtTokenInfoDTO.builder()
                    .userId(userId)
                    .token(token)
                    .createdAt(createdAtLocal)
                    .expiresAt(expiresAtLocal)
                    .build();
                    
        } catch (JwtException e) {
            // 重新抛出自定义异常
            throw e;
        } catch (Exception e) {
            log.error("获取JWT令牌信息失败: {}", e.getMessage());
            throw new JwtException("INVALID_TOKEN", "无法获取JWT令牌信息", e, 400);
        }
    }
    
    /**
     * 从Authorization头中提取并获取JWT令牌信息
     *
     * @param authHeader Authorization头
     * @return JWT令牌信息DTO，如果令牌无效则返回null
     */
    public JwtTokenInfoDTO extractAndGetTokenInfo(String authHeader) {
        try {
            String token = TokenUtil.extractTokenFromHeader(authHeader);
            if (token == null) {
                return null;
            }
            
            if (!validateToken(token)) {
                return null;
            }
            
            return getTokenInfo(token);
        } catch (Exception e) {
            log.warn("从Authorization头中提取JWT令牌信息失败: {}", e.getMessage());
            return null;
        }
    }
}

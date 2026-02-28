package top.fblue.watermelon.auth.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.fblue.common.exception.BusinessException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Token工具类
 * 统一处理token提取和验证逻辑
 */
@Slf4j
public class TokenUtil {

    private static final String USER_ID_KEY = "userId";

    /**
     * 本地解析 JWT，返回 Claims（包含 userId、jti、exp 等）
     * 若 token 过期或签名非法，抛出异常
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Claims 中获取 userId
     */
    public static Long getUserId(Claims claims) {
        return claims.get(USER_ID_KEY, Long.class);
    }

    /**
     * 从 Claims 中获取 jti（JWT 唯一标识）
     * 若 JWT 未设置 jti，则返回 null
     */
    public static String getJti(Claims claims) {
        return claims.getId();
    }

    /**
     * 计算 token 剩余有效时间（秒）
     * 若已过期则返回 0
     */
    public static long getRemainingSeconds(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return 0L;
        }
        long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0L);
    }

    /**
     * 从Authorization头中提取token
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }

        String token;

        // 支持Bearer token格式
        // Basic认证：用于用户名密码
        // Bearer认证：用于访问令牌
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            // 也支持直接传递token
            token = authHeader.trim();
        }
        if (token.isEmpty()) {
            throw new BusinessException("Token不能为空");
        }
        return token;
    }

    /**
     * 从HttpServletRequest中提取token
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        // 优先从Authorization头中提取
        String authHeader = request.getHeader("Authorization");
        return extractTokenFromHeader(authHeader);
    }
} 
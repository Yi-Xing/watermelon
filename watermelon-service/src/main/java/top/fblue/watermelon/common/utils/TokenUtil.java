package top.fblue.watermelon.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Token工具类
 * 统一处理token提取和验证逻辑
 */
public class TokenUtil {

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
            throw new IllegalArgumentException("Token不能为空");
        }
        return token;
    }

    /**
     * 从HttpServletRequest中提取token
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        // 优先从Authorization头中提取
        String authHeader = request.getHeader("Authorization");
        String token = extractTokenFromHeader(authHeader);
        if (StringUtils.hasText(token)) {
            return token;
        }

        // 从请求参数中提取
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam.trim();
        }

        return null;
    }
} 
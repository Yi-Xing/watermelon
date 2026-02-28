package top.fblue.watermelon.auth.infrastructure.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.watermelon.auth.application.service.UserAuthApplicationService;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.common.utils.TokenUtil;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static top.fblue.watermelon.auth.common.constant.UserConst.AUTH_STATE_COOKIE;
import static top.fblue.watermelon.auth.common.constant.UserConst.CURRENT_USER_KEY;

/**
 * Token 认证拦截器
 * <p>
 * 1. 从 header 获取 token
 * 2. 调用 UserAuthApplicationService.validateToken() 完成验证（JWT 解析 + 黑名单检查）
 * 3. 验证成功：将用户信息写入 request
 * 4. 验证失败：生成 state 写入 cookie，返回 401 + 登录地址
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthInterceptor implements HandlerInterceptor {

    private final UserAuthApplicationService userAuthApplicationService;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 1. 从 Authorization header 获取 token
        String token = TokenUtil.extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            log.debug("请求无 token: {}", requestURI);
            handleUnauthorized(response);
            return false;
        }

        // 2. 委托给应用服务完成验证（JWT 解析 + 黑名单检查）
        UserDTO userDTO;
        try {
            userDTO = userAuthApplicationService.validateToken(token);
        } catch (Exception e) {
            log.warn("token 验证失败, uri: {}, error: {}", requestURI, e.getMessage());
            handleUnauthorized(response);
            return false;
        }

        // 3. 将用户信息写入 request，供后续拦截器和 Controller 使用
        request.setAttribute(CURRENT_USER_KEY, userDTO);
        log.debug("token 验证成功，userId: {}, jti: {}", userDTO.getUserId(), userDTO.getJti());
        return true;
    }

    /**
     * 验证失败：生成 state 写入 cookie，返回 401 + 登录地址
     */
    private void handleUnauthorized(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");

        Cookie stateCookie = new Cookie(AUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setPath("/");
        stateCookie.setMaxAge(600);
        response.addCookie(stateCookie);

        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("message", "未登录或 token 已过期");
        body.put("loginUrl", authProperties.getLoginUrl() + "?state=" + state);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

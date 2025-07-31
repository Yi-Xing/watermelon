package top.fblue.watermelon.infrastructure.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.watermelon.common.utils.TokenUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.TokenDomainService;

/**
 * 认证拦截器
 * 验证请求中的token有效性
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    @Resource
    private TokenDomainService tokenDomainService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 获取token
        String token = TokenUtil.extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            log.warn("请求缺少token: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 验证token
        User user = tokenDomainService.validateToken(token);
        if (user == null) {
            log.warn("Token验证失败: {}", token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 将用户信息存储到请求属性中，供后续使用
        request.setAttribute("currentUser", user);
        log.debug("Token验证成功，用户ID: {}", user.getId());
        
        return true;
    }

} 
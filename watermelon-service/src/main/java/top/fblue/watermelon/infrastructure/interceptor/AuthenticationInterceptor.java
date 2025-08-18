package top.fblue.watermelon.infrastructure.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.utils.TokenUtil;
import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.service.TokenDomainService;

import static top.fblue.watermelon.common.constant.UserConst.CURRENT_USER_KEY;

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
        UserToken userToken;
        String token = "";
        try {
            token = TokenUtil.extractTokenFromRequest(request);
            // 验证token
            userToken = tokenDomainService.validateToken(token);
        } catch (Exception e) {
            log.warn("请求token不合法: {}，{}", requestURI, token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        UserTokenDTO tokenDTO = UserTokenDTO
                .builder()
                .userId(userToken.getUserId())
                .token(userToken.getToken())
                .createdTime(userToken.getCreatedTime())
                .expireTime(userToken.getExpireTime())
                .build();
        // 将用户信息存储到请求属性中，供后续使用
        request.setAttribute(CURRENT_USER_KEY, tokenDTO);
        log.debug("Token验证成功，用户ID: {}", userToken.getUserId());
        return true;
    }

} 
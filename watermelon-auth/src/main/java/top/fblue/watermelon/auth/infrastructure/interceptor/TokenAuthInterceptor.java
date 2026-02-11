package top.fblue.watermelon.auth.infrastructure.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.watermelon.auth.application.service.AuthCommonApplicationService;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.common.utils.TokenUtil;

import static top.fblue.watermelon.auth.common.constant.UserConst.CURRENT_USER_KEY;


/**
 * Token认证拦截器
 * 专门负责验证请求中的token有效性
 */
@Slf4j
@Component
public class TokenAuthInterceptor implements HandlerInterceptor {

    @Resource
    private AuthCommonApplicationService authCommonApplicationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 获取token
        UserDTO userDTO;
        String token = "";
        try {
            token = TokenUtil.extractTokenFromRequest(request);
            // 验证token
            userDTO = authCommonApplicationService.validateToken(token);
        } catch (Exception e) {
            log.warn("请求token不合法: {}，{}", requestURI, token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 将用户信息存储到请求属性中，供后续拦截器使用
        request.setAttribute(CURRENT_USER_KEY, userDTO);

        log.debug("Token验证成功，用户ID: {}", userDTO.getUserId());
        return true;
    }
}

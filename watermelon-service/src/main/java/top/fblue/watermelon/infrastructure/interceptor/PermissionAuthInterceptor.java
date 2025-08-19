package top.fblue.watermelon.infrastructure.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.watermelon.application.service.AuthApplicationService;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.infrastructure.config.JwtConfig;
import top.fblue.watermelon.infrastructure.config.SystemConfig;

import static top.fblue.watermelon.common.constant.UserConst.CURRENT_USER_KEY;

/**
 * 权限验证拦截器
 * 专门负责验证用户是否有访问指定接口的权限
 */
@Slf4j
@Component
public class PermissionAuthInterceptor implements HandlerInterceptor {

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private AuthApplicationService authApplicationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 从请求属性中获取用户信息（由Token认证拦截器设置）
        UserTokenDTO userToken = (UserTokenDTO) request.getAttribute(CURRENT_USER_KEY);
        if (userToken == null) {
            log.warn("用户信息未找到，请确保Token认证拦截器已执行");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 构建资源代码用于权限验证
        String resourceCode = systemConfig.getCode() + ":" + method + ":" + requestURI;
        log.debug("用户 {} 请求资源: {}", userToken.getUserId(), resourceCode);

        // 判断用户是否有请求接口的权限
        if (!authApplicationService.hasPermission(resourceCode)) {
            log.warn("用户 {} 没有访问 {} {} 的权限", userToken.getUserId(), method, requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        log.debug("权限验证成功，用户ID: {}", userToken.getUserId());
        return true;
    }
}

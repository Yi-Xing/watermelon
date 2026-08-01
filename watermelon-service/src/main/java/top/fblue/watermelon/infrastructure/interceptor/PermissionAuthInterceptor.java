package top.fblue.watermelon.infrastructure.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.watermelon.application.service.UserAuthApplicationService;
import top.fblue.watermelon.infrastructure.config.SystemConfig;

/**
 * 权限验证拦截器
 * 专门负责验证用户是否有访问指定接口的权限
 */
@Slf4j
@Component
public class PermissionAuthInterceptor implements HandlerInterceptor {

    /** 当前系统编码配置，用于构造接口资源编码。 */
    @Resource
    private SystemConfig systemConfig;

    /** 用户身份及资源权限应用服务。 */
    @Resource
    private UserAuthApplicationService userAuthApplicationService;

    /**
     * 根据当前用户、HTTP 方法和请求路径校验接口资源权限。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 当前请求处理器
     * @return 允许继续处理时返回 {@code true}，鉴权失败时返回 {@code false}
     * @throws Exception 拦截器处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 从请求属性中获取用户信息（由Token认证拦截器设置）
        SsoPrincipal principal = (SsoPrincipal) request.getAttribute(SsoConstants.CURRENT_USER_ATTRIBUTE);
        if (principal == null) {
            log.warn("用户信息未找到，请确保Token认证拦截器已执行");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 构建资源代码用于权限验证
        String resourceCode = systemConfig.getCode() + ":" + method + ":" + requestURI;
        log.debug("用户 {} 请求资源: {}", principal.getUserId(), resourceCode);

        // 判断用户是否有请求接口的权限
        if (!userAuthApplicationService.hasPermission(resourceCode)) {
            log.warn("用户 {} 没有访问 {} {} 的权限", principal.getUserId(), method, requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        log.debug("权限验证成功，用户ID: {}", principal.getUserId());
        return true;
    }
}

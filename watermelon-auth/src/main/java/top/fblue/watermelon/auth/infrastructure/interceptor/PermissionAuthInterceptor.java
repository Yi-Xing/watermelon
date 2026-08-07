package top.fblue.watermelon.auth.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.watermelon.auth.application.service.PermissionApplicationService;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

/**
 * 当前系统 HTTP 接口权限校验拦截器。
 *
 * <p>拦截器只负责从请求中提取身份和构造接口资源编码，实际权限判断统一委托给
 * auth 应用服务；具体拦截路径仍由宿主 service 模块注册。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionAuthInterceptor implements HandlerInterceptor {

    /** Auth 模块及当前服务端 Client 配置。 */
    private final AuthProperties authProperties;

    /** 运行时权限应用服务。 */
    private final PermissionApplicationService permissionApplicationService;

    /**
     * 根据当前用户、HTTP 方法和请求路径校验接口资源权限。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 当前请求处理器
     * @return 允许继续处理时返回 {@code true}，鉴权失败时返回 {@code false}
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 获取认证拦截器写入请求的用户身份
        SsoPrincipal principal = (SsoPrincipal) request.getAttribute(SsoConstants.CURRENT_USER_ATTRIBUTE);
        if (principal == null) {
            log.warn("用户信息未找到，请确保 Token 认证拦截器已执行");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 2. 根据当前系统、HTTP 方法和请求路径构造完整接口权限编码
        String systemCode = authProperties.getServerClientId();
        String resourceCode = systemCode + ":" + request.getMethod() + ":" + request.getRequestURI();
        log.debug("用户 {} 请求资源: {}", principal.getUserId(), resourceCode);

        // 3. 委托运行时鉴权应用服务判断接口权限
        if (!permissionApplicationService.hasApiPermission(principal.getUserId(), systemCode, resourceCode)) {
            log.warn("用户 {} 没有访问 {} {} 的权限",
                    principal.getUserId(), request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // 4. 鉴权通过，继续执行请求链
        return true;
    }
}

package top.fblue.watermelon.infrastructure.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.fblue.watermelon.auth.infrastructure.interceptor.PermissionAuthInterceptor;
import top.fblue.watermelon.infrastructure.interceptor.TraceInterceptor;

/**
 * Web MVC配置类
 * 配置拦截器链：Token认证 → 权限验证
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 接口资源权限校验拦截器。 */
    @Resource
    private PermissionAuthInterceptor permissionAuthInterceptor;

    /** 请求链路追踪上下文拦截器。 */
    @Resource
    private TraceInterceptor traceInterceptor;

    /**
     * 注册链路追踪和接口权限拦截器，并明确它们与 SSO 拦截器的执行顺序。
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 设置 TraceContext
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        // 2. SSO Token 认证由 water-auth 自动配置，order=10。
        // 3. 权限验证拦截器
        registry.addInterceptor(permissionAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(20);
    }
}

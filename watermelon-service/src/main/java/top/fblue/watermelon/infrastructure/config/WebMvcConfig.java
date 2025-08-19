package top.fblue.watermelon.infrastructure.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.fblue.watermelon.infrastructure.interceptor.TokenAuthInterceptor;
import top.fblue.watermelon.infrastructure.interceptor.PermissionAuthInterceptor;

/**
 * Web MVC配置类
 * 配置拦截器链：Token认证 → 权限验证
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Resource
    private TokenAuthInterceptor tokenAuthInterceptor;
    
    @Resource
    private PermissionAuthInterceptor permissionAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. Token认证拦截器（第一优先级）
        registry.addInterceptor(tokenAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login")
                .order(1);
        
        // 2. 权限验证拦截器（第二优先级）
        registry.addInterceptor(permissionAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(2);
    }
}

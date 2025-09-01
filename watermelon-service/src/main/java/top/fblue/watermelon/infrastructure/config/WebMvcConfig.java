package top.fblue.watermelon.infrastructure.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.fblue.watermelon.infrastructure.interceptor.TokenAuthInterceptor;
import top.fblue.watermelon.infrastructure.interceptor.PermissionAuthInterceptor;
import top.fblue.watermelon.infrastructure.interceptor.TraceInterceptor;

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

    @Resource
    private TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 设置 TraceContext
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        // 2. Token认证拦截器
        registry.addInterceptor(tokenAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login")
                .order(2);
        
        // 3. 权限验证拦截器
//        registry.addInterceptor(permissionAuthInterceptor)
//                .addPathPatterns("/api/admin/**")
//                .order(3);
    }
}

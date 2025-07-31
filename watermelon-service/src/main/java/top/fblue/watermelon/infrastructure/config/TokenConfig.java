package top.fblue.watermelon.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;
import top.fblue.watermelon.infrastructure.repository.UserTokenRepositoryImpl;

/**
 * Token配置类
 * 用于配置Token存储的实现方式
 */
@Configuration
public class TokenConfig {
    
    /**
     * 默认使用Map存储实现
     * 后期可以轻松切换为Redis实现
     * Primary 当有多个相同类型的Bean时，标记这个Bean为主要的（优先的）。如果注入UserTokenRepository，Spring会优先使用这个Bean。
     */
    @Bean
    @Primary
    public UserTokenRepository userTokenRepository() {
        return new UserTokenRepositoryImpl();
    }
    
    /**
     * 如果要切换到Redis实现，可以：
     * 1. 添加Redis依赖
     * 2. 创建RedisUserTokenRepositoryImpl
     * 3. 将上面的方法改为：
     *    return new RedisUserTokenRepositoryImpl();
     * 示例：Redis实现（需要添加Redis依赖）
     */
    /*
    @Bean
    @Primary
    public UserTokenRepository userTokenRepository() {
        return new RedisUserTokenRepositoryImpl();
    }
    */
} 
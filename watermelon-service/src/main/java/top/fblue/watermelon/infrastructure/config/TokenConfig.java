package top.fblue.watermelon.infrastructure.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;
import top.fblue.watermelon.infrastructure.repository.UserTokenJwtRepositoryImpl;
import top.fblue.watermelon.infrastructure.repository.UserTokenRepositoryImpl;

/**
 * Token配置类
 * 用于配置Token存储的实现方式
 */
@Configuration
public class TokenConfig {

    @Resource
    private JwtConfig jwtConfig;
    /**
     * 默认使用Map存储实现，可以使用jwt。或者 创建RedisUserTokenRepositoryImpl使用redis存储。
     * Primary 当有多个相同类型的Bean时，标记这个Bean为主要的（优先的）。如果注入UserTokenRepository，Spring会优先使用这个Bean。
     */
    @Bean
    @Primary
    public UserTokenRepository userTokenRepository() {
        if (jwtConfig.getEnable()){
            return new UserTokenJwtRepositoryImpl();
        }else {
            return new UserTokenRepositoryImpl();
        }
    }
}
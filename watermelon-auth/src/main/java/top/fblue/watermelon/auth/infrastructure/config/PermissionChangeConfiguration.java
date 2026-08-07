package top.fblue.watermelon.auth.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 权限变更事务发件箱调度配置。
 *
 * <p>权限领域服务使用的 {@code Clock} 由 Water 统一自动配置。</p>
 */
@Configuration
@EnableScheduling
public class PermissionChangeConfiguration {
}

package top.fblue.watermelon.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.repository.SsoUserLoader;
import top.fblue.watermelon.domain.user.service.UserDomainService;

/**
 * 基于用户领域服务的 SSO 用户加载适配器。
 */
@Component
@RequiredArgsConstructor
public class SsoUserLoaderImpl implements SsoUserLoader {

    /** 用户领域服务，用于查询用户中心中的用户数据。 */
    private final UserDomainService userDomainService;

    /**
     * {@inheritDoc}
     */
    @Override
    public User loadUser(Long userId) {
        var sourceUser = userDomainService.getUserById(userId);
        if (sourceUser == null) {
            return null;
        }
        return User.builder()
                .id(sourceUser.getId())
                .username(sourceUser.getUsername())
                .build();
    }
}

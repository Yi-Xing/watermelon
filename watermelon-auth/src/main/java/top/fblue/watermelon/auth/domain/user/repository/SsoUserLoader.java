package top.fblue.watermelon.auth.domain.user.repository;

import top.fblue.watermelon.auth.domain.user.entity.User;

/**
 * SSO 用户加载端口，由业务服务模块实现以隔离具体的用户持久化方式。
 */
public interface SsoUserLoader {

    /**
     * 根据用户 ID 加载 SSO 签发流程所需的用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息；用户不存在时返回 {@code null}
     */
    User loadUser(Long userId);
}

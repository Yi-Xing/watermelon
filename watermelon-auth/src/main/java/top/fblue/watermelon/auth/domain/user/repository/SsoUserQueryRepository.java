package top.fblue.watermelon.auth.domain.user.repository;

import top.fblue.watermelon.auth.domain.user.entity.User;

/**
 * SSO 用户查询仓储，由业务服务模块提供具体实现。
 */
public interface SsoUserQueryRepository {

    /**
     * 根据用户 ID 查询 SSO 签发流程所需的用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息；用户不存在时返回 {@code null}
     */
    User findById(Long userId);
}

package top.fblue.watermelon.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.repository.SsoUserQueryRepository;
import top.fblue.watermelon.infrastructure.converter.SsoUserPOConverter;
import top.fblue.watermelon.infrastructure.mapper.UserMapper;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * SSO 用户查询仓储实现。
 *
 * <p>只读取 SSO 所需的用户 ID 和名称，避免认证流程依赖用户领域服务
 * 或完整的用户聚合仓储。</p>
 */
@Repository
@RequiredArgsConstructor
public class SsoUserQueryRepositoryImpl implements SsoUserQueryRepository {

    /** 用户数据访问 Mapper，仅查询 SSO 所需字段。 */
    private final UserMapper userMapper;

    /** 用户持久化对象与 SSO 用户对象转换器。 */
    private final SsoUserPOConverter userConverter;

    /**
     * 查询 SSO 所需的最小用户信息；用户不存在时返回 {@code null}。
     *
     * @param userId 用户 ID
     * @return SSO 流程所需的最小用户信息；用户不存在时返回 {@code null}
     */
    @Override
    public User findById(Long userId) {
        // 1. 查询 SSO 流程所需的最小用户字段
        UserPO userPO = userMapper.selectSsoUserById(userId);

        // 2. 转换为认证领域用户并返回
        return userConverter.toSsoUser(userPO);
    }
}

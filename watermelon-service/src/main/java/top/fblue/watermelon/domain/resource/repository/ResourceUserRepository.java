package top.fblue.watermelon.domain.resource.repository;

import top.fblue.watermelon.domain.user.entity.User;

/**
 * 用户仓储接口（资源领域内使用）
 * 用于资源领域内查询用户相关信息
 */
public interface ResourceUserRepository {

    /**
     * 根据ID查找用户
     */
    User findById(Long id);
}
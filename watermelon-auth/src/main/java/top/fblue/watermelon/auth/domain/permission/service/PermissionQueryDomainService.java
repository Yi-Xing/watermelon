package top.fblue.watermelon.auth.domain.permission.service;

import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;

/**
 * 权限查询领域服务。
 */
public interface PermissionQueryDomainService {

    /**
     * 查询用户在指定系统中的有效权限快照。
     *
     * @param userId 用户 ID
     * @param systemCode 业务系统编码
     * @return 权限领域快照
     */
    PermissionSnapshot getPermissionSnapshot(Long userId, String systemCode);

    /**
     * 判断用户是否拥有指定接口权限。
     *
     * @param userId 用户 ID
     * @param systemCode 业务系统编码
     * @param resourceCode 完整接口资源编码
     * @return 是否拥有接口权限
     */
    boolean hasApiPermission(Long userId, String systemCode, String resourceCode);
}

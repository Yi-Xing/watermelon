package top.fblue.watermelon.auth.domain.permission.repository;

/**
 * 维护用户级和系统级权限版本。
 */
public interface PermissionVersionRepository {

    /**
     * 查询用户权限版本。
     *
     * @param userId 用户 ID
     * @return 用户权限版本
     */
    long getUserPermissionVersion(Long userId);

    /**
     * 原子递增用户权限版本。
     *
     * @param eventId 幂等事件 ID
     * @param userId 用户 ID
     * @return 递增后的用户权限版本
     */
    long incrementUserPermissionVersion(String eventId, Long userId);

    /**
     * 查询系统权限版本。
     *
     * @param systemCode 系统编码
     * @return 系统权限版本
     */
    long getSystemPermissionVersion(String systemCode);

    /**
     * 原子递增系统权限版本。
     *
     * @param eventId 幂等事件 ID
     * @param systemCode 系统编码
     * @return 递增后的系统权限版本
     */
    long incrementSystemPermissionVersion(String eventId, String systemCode);
}

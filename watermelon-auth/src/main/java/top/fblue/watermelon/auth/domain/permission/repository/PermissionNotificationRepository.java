package top.fblue.watermelon.auth.domain.permission.repository;

import java.util.List;

/**
 * 向已接入用户中心的业务系统发送权限缓存失效通知。
 */
public interface PermissionNotificationRepository {

    /**
     * 查询当前启用且配置了通知地址的业务系统。
     *
     * @return 可通知业务系统编码列表
     */
    List<String> findNotifiableSystemCodes();

    /**
     * 通知指定业务系统失效用户权限缓存。
     *
     * @param systemCode 目标系统编码
     * @param eventId 幂等事件 ID
     * @param userId 受影响用户 ID
     * @param userPermissionVersion 最新用户维度权限版本
     * @param systemPermissionVersion 当前系统维度权限版本
     */
    void notifyUserChanged(String systemCode,
                           String eventId,
                           Long userId,
                           long userPermissionVersion,
                           long systemPermissionVersion);

    /**
     * 通知指定业务系统切换系统级权限缓存版本。
     *
     * @param systemCode 目标系统编码
     * @param eventId 幂等事件 ID
     * @param systemPermissionVersion 最新系统维度权限版本
     */
    void notifySystemChanged(String systemCode, String eventId, long systemPermissionVersion);
}

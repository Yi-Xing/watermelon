package top.fblue.watermelon.auth.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.api.request.PermissionChangeRequest;

/**
 * 权限变更通知请求转换器。
 */
@Component
public class PermissionChangeRequestConverter {

    /**
     * 构造用户级权限变更通知。
     *
     * @param eventId 事件 ID
     * @param systemCode 目标系统编码
     * @param userId 受影响用户 ID
     * @param userPermissionVersion 用户维度权限版本
     * @param systemPermissionVersion 系统维度权限版本
     * @return 用户级权限变更通知
     */
    public PermissionChangeRequest toUserChangedRequest(String eventId,
                                                        String systemCode,
                                                        Long userId,
                                                        long userPermissionVersion,
                                                        long systemPermissionVersion) {
        return PermissionChangeRequest.builder()
                .eventId(eventId)
                .systemCode(systemCode)
                .userId(userId)
                .userPermissionVersion(userPermissionVersion)
                .systemPermissionVersion(systemPermissionVersion)
                .build();
    }

    /**
     * 构造系统级权限变更通知。
     *
     * @param eventId 事件 ID
     * @param systemCode 目标系统编码
     * @param systemPermissionVersion 系统维度权限版本
     * @return 系统级权限变更通知
     */
    public PermissionChangeRequest toSystemChangedRequest(String eventId,
                                                          String systemCode,
                                                          long systemPermissionVersion) {
        return PermissionChangeRequest.builder()
                .eventId(eventId)
                .systemCode(systemCode)
                .systemPermissionVersion(systemPermissionVersion)
                .build();
    }
}

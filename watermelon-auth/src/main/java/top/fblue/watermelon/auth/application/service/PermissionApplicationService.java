package top.fblue.watermelon.auth.application.service;

import top.fblue.watermelon.api.request.PermissionSnapshotRequest;
import top.fblue.watermelon.api.response.PermissionSnapshotResponse;

/**
 * 对外权限快照应用服务。
 */
public interface PermissionApplicationService {

    /**
     * 查询用户在指定业务系统中的有效权限快照。
     *
     * @param request 权限快照查询请求
     * @return 页面、按钮、接口权限及版本信息
     */
    PermissionSnapshotResponse getPermissionSnapshot(PermissionSnapshotRequest request);

    /**
     * 判断用户是否拥有目标系统中的指定接口权限。
     *
     * @param userId 用户 ID
     * @param systemCode 目标系统编码
     * @param resourceCode 完整接口资源编码
     * @return 是否拥有接口权限
     */
    boolean hasApiPermission(Long userId, String systemCode, String resourceCode);
}

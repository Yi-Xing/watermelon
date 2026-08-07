package top.fblue.watermelon.api;

import top.fblue.common.annotation.RpcPublic;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.request.PermissionSnapshotRequest;
import top.fblue.watermelon.api.response.PermissionSnapshotResponse;

/**
 * 用户中心对外提供的权限快照查询接口。
 */
public interface PermissionRpc {

    /**
     * 查询指定用户在调用方系统内的有效页面、按钮和接口权限。
     *
     * @param request 用户、系统标识
     * @return 当前有效权限快照
     */
    @RpcPublic
    ApiResponse<PermissionSnapshotResponse> getPermissionSnapshot(PermissionSnapshotRequest request);
}

package top.fblue.watermelon.api;

import top.fblue.common.annotation.RpcPublic;
import top.fblue.watermelon.api.request.PermissionChangeRequest;

/**
 * 用户中心向各业务系统发送权限缓存失效通知的回调接口。
 */
public interface SystemPermissionChangeRpc {

    /**
     * 通知业务系统失效用户级或系统级权限缓存。
     *
     * @param request 权限变更通知；userId 为空表示失效整个系统
     */
    @RpcPublic
    void permissionChanged(PermissionChangeRequest request);
}

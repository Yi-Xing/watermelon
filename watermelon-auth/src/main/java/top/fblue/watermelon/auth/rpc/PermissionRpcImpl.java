package top.fblue.watermelon.auth.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.PermissionRpc;
import top.fblue.watermelon.api.request.PermissionSnapshotRequest;
import top.fblue.watermelon.api.response.PermissionSnapshotResponse;
import top.fblue.watermelon.auth.application.service.PermissionApplicationService;
import top.fblue.watermelon.auth.rpc.validator.PermissionRpcRequestValidator;

/**
 * 用户中心权限快照 RPC 实现。
 */
@DubboService
@RequiredArgsConstructor
public class PermissionRpcImpl implements PermissionRpc {

    /** 权限快照应用服务。 */
    private final PermissionApplicationService permissionApplicationService;

    /** 权限 RPC 请求和调用方校验器。 */
    private final PermissionRpcRequestValidator requestValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<PermissionSnapshotResponse> getPermissionSnapshot(PermissionSnapshotRequest request) {
        // 1. 统一校验请求参数、客户端注册状态和 Dubbo 调用方身份
        requestValidator.validateSnapshotRequest(request);

        // 2. 查询权限快照并封装统一响应
        return ApiResponse.success(permissionApplicationService.getPermissionSnapshot(request));
    }
}

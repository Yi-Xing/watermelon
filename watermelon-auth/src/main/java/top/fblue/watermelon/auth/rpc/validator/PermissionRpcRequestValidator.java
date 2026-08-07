package top.fblue.watermelon.auth.rpc.validator;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.PermissionSnapshotRequest;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

/**
 * 权限 RPC 请求校验器。
 */
@Component
@RequiredArgsConstructor
public class PermissionRpcRequestValidator {

    /** 已注册的 SSO 客户端配置。 */
    private final AuthProperties authProperties;

    /**
     * 统一校验权限快照请求参数和 Dubbo 调用方身份。
     *
     * @param request 权限快照请求
     */
    public void validateSnapshotRequest(PermissionSnapshotRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId() <= 0
                || !StringUtils.hasText(request.getSystemCode())) {
            throw new SsoAuthException(ApiCodeEnum.BAD_REQUEST, "用户和系统编码不能为空");
        }

        String systemCode = request.getSystemCode();
        String callerApplication = RpcContext.getServiceContext().getRemoteApplicationName();
        AuthProperties.Client client = authProperties.getClients().get(systemCode);
        if (!StringUtils.hasText(callerApplication)
                || !callerApplication.equals(systemCode)
                || client == null
                || !client.isEnabled()) {
            throw new SsoAuthException(ApiCodeEnum.FORBIDDEN,
                    "RPC 调用方无权查询该系统权限");
        }
    }
}

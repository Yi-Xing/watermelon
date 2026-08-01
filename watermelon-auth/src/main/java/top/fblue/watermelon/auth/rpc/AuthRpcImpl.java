package top.fblue.watermelon.auth.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.util.StringUtils;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.AuthRpc;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;

/**
 * Auth RPC 服务实现
 */
@DubboService
@RequiredArgsConstructor
public class AuthRpcImpl implements AuthRpc {

    /** SSO 授权应用服务。 */
    private final SsoAuthorizationApplicationService authorizationApplicationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<CodeExchangeResponse> exchangeCode(CodeExchangeRequest request) {
        validateCaller(request == null ? null : request.getClientId());
        return ApiResponse.success(authorizationApplicationService.exchangeCode(request));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<Void> logout(LogoutRpcRequest request) {
        validateCaller(request == null ? null : request.getClientId());
        authorizationApplicationService.revokeSession(request);
        return ApiResponse.success(null);
    }

    /**
     * Dubbo 调用方应用名必须与请求中的 clientId 一致，防止 Client 伪造其他系统身份。
     *
     * @param clientId 请求声明的 SSO 客户端标识
     */
    private void validateCaller(String clientId) {
        String callerApplication = RpcContext.getServiceContext().getRemoteApplicationName();
        if (!StringUtils.hasText(clientId)
                || !StringUtils.hasText(callerApplication)
                || !callerApplication.equals(clientId)) {
            throw new SsoAuthException(ApiCodeEnum.FORBIDDEN,
                    "RPC 调用方与 clientId 不匹配");
        }
    }
}

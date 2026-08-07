package top.fblue.watermelon.auth.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.AuthRpc;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;
import top.fblue.watermelon.auth.rpc.validator.AuthRpcRequestValidator;

/**
 * Auth RPC 服务实现
 */
@DubboService
@RequiredArgsConstructor
public class AuthRpcImpl implements AuthRpc {

    /** SSO 授权应用服务。 */
    private final SsoAuthorizationApplicationService authorizationApplicationService;

    /** SSO RPC 请求及调用方校验器。 */
    private final AuthRpcRequestValidator requestValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<CodeExchangeResponse> exchangeCode(CodeExchangeRequest request) {
        // 1. 统一校验请求参数和 Dubbo 调用方身份
        requestValidator.validateCodeExchangeRequest(request);

        // 2. 兑换授权码并封装统一响应
        return ApiResponse.success(authorizationApplicationService.exchangeCode(request));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<Void> logout(LogoutRpcRequest request) {
        // 1. 统一校验请求参数和 Dubbo 调用方身份
        requestValidator.validateLogoutRequest(request);

        // 2. 注销全局会话
        authorizationApplicationService.revokeSession(request);
        return ApiResponse.success(null);
    }
}

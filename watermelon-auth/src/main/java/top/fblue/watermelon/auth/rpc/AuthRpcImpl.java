package top.fblue.watermelon.auth.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.AuthRpc;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.service.UserAuthApplicationService;

/**
 * Auth RPC 服务实现
 */
@DubboService
@RequiredArgsConstructor
public class AuthRpcImpl implements AuthRpc {

    private final UserAuthApplicationService userAuthApplicationService;

    @Override
    public ApiResponse<CodeExchangeResponse> exchangeCode(CodeExchangeRequest request) {
        return ApiResponse.success(userAuthApplicationService.exchangeCode(request));
    }

    @Override
    public ApiResponse<Void> logout(LogoutRpcRequest request) {
        userAuthApplicationService.revokeToken(request);
        return ApiResponse.success(null);
    }
}

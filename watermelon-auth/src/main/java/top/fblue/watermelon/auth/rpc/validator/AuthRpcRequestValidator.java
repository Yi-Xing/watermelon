package top.fblue.watermelon.auth.rpc.validator;

import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;

/**
 * SSO 认证 RPC 请求校验器。
 */
@Component
public class AuthRpcRequestValidator {

    /**
     * 统一校验授权码兑换请求及 Dubbo 调用方身份。
     *
     * @param request 授权码兑换请求
     */
    public void validateCodeExchangeRequest(CodeExchangeRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getClientId())
                || !StringUtils.hasText(request.getRedirectUri())) {
            throw new SsoAuthException(ApiCodeEnum.BAD_REQUEST, "授权码兑换参数不完整");
        }
        validateCaller(request.getClientId());
    }

    /**
     * 统一校验全局注销请求及 Dubbo 调用方身份。
     *
     * @param request 全局注销请求
     */
    public void validateLogoutRequest(LogoutRpcRequest request) {
        if (request == null || !StringUtils.hasText(request.getSid())
                || !StringUtils.hasText(request.getClientId())) {
            throw new SsoAuthException(ApiCodeEnum.BAD_REQUEST, "sid 和 clientId 不能为空");
        }
        validateCaller(request.getClientId());
    }

    /**
     * 校验 Dubbo 调用方应用名是否与请求中的客户端标识一致。
     *
     * @param clientId 请求声明的客户端标识
     */
    private void validateCaller(String clientId) {
        String callerApplication = RpcContext.getServiceContext().getRemoteApplicationName();
        if (!StringUtils.hasText(callerApplication) || !callerApplication.equals(clientId)) {
            throw new SsoAuthException(ApiCodeEnum.FORBIDDEN,
                    "RPC 调用方与 clientId 不匹配");
        }
    }
}

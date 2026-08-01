package top.fblue.watermelon.auth.application.service;

import top.fblue.auth.context.SsoPrincipal;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;

/**
 * 用户中心 SSO 授权服务。
 */
public interface SsoAuthorizationApplicationService {

    /**
     * 为已登录用户创建全局 SSO 会话。
     *
     * @param userId 用户 ID
     * @param deviceCode 登录设备标识
     * @return 新建的全局会话信息
     */
    SsoSessionInfo createSession(Long userId, String deviceCode);

    /**
     * 校验客户端和回调地址，为当前全局会话签发一次性授权码。
     *
     * @param principal 当前 SSO 登录主体
     * @param request 客户端授权请求
     * @return 客户端回调信息
     */
    CallbackResponse generateCallback(SsoPrincipal principal, CallbackRequest request);

    /**
     * 消费一次性授权码并返回客户端建立本地登录态所需的信息。
     *
     * @param request 授权码兑换请求
     * @return 授权码对应的用户及全局会话信息
     */
    CodeExchangeResponse exchangeCode(CodeExchangeRequest request);

    /**
     * 注销全局 SSO 会话，并通知已绑定该会话的客户端撤销本地登录态。
     *
     * @param request 全局会话注销请求
     */
    void revokeSession(LogoutRpcRequest request);
}

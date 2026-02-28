package top.fblue.watermelon.auth.application.service;

import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;

/**
 * 鉴权应用服务接口
 */
public interface UserAuthApplicationService {

    /**
     * 验证 token 有效性并获取 UserDTO
     */
    UserDTO validateToken(String token);

    /**
     * 生成回调地址
     * <p>
     * 校验 return_url 域名白名单、校验 state 与 cookieState 一致，
     * 生成一次性授权码并拼接到回调地址后返回。
     *
     * @param userId      当前登录用户ID
     * @param jti         当前 token 的 jti
     * @param expiresIn   token 剩余有效时间（秒）
     * @param returnUrl   回调地址（需在白名单内）
     * @param state       请求中的 state
     * @param cookieState cookie 中的 state
     * @return 拼接了 code 和 state 的回调地址
     */
    CallbackResponse generateCallback(Long userId, String jti, long expiresIn,
                                      String returnUrl, String state, String cookieState);

    /**
     * 授权码换用户信息
     *
     * @param request code、callerSystem
     * @return 用户信息、jti、expiresIn
     */
    CodeExchangeResponse exchangeCode(CodeExchangeRequest request);

    /**
     * 退出登录（吊销 jti，并通知各关联系统）
     *
     * @param request jti、expiresIn、deviceCode
     */
    void revokeToken(LogoutRpcRequest request);
}

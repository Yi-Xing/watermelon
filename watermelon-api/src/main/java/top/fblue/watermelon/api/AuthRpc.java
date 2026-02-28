package top.fblue.watermelon.api;

import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;

/**
 * Auth 对外 Dubbo RPC 接口
 * <p>
 * 其他系统（系统A、系统B）引入 watermelon-api 依赖后，
 * 通过 @DubboReference 调用此接口。
 */
public interface AuthRpc {

    /**
     * 使用授权码（code）换取用户信息
     * <p>
     * 调用逻辑：
     * 1. 使用 code 查询 Redis，获取 userId、jti、expiresIn
     * 2. 删除 Redis 中的 code（一次性）
     * 3. 记录 jti -> callerSystem（用于退出登录时通知）
     * 4. 查询用户信息
     *
     * @param request code 和调用方系统标识
     * @return 用户信息、jti、剩余有效时间
     */
    ApiResponse<CodeExchangeResponse> exchangeCode(CodeExchangeRequest request);

    /**
     * 退出登录（吊销 jti）
     * <p>
     * 调用逻辑：
     * 1. 将 jti 加入 Redis 黑名单，TTL = expiresIn
     * 2. 查询 jti 关联的所有系统
     * 3. 逐一调用各系统的 {@link SystemTokenRevokeRpc#revokeToken} 同步通知
     *    （失败只打印 error 日志，不影响主流程；后续可改为 MQ 异步通知）
     *
     * @param request jti、expiresIn、deviceCode
     */
    ApiResponse<Void> logout(LogoutRpcRequest request);
}

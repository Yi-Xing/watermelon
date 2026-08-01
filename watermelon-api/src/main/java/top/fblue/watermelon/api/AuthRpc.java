package top.fblue.watermelon.api;

import top.fblue.common.annotation.RpcPublic;
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
     * 1. 原子消费 code，获取 userId、sid、sessionExpireAt
     * 2. 删除 Redis 中的 code（一次性）
     * 3. 记录 sid -> clientId（用于退出登录时通知）
     * 4. 查询用户信息
     *
     * @param request code 和调用方系统标识
     * @return 用户信息、sid、全局会话过期时间
     */
    @RpcPublic
    ApiResponse<CodeExchangeResponse> exchangeCode(CodeExchangeRequest request);

    /**
     * 全局退出登录（吊销 sid）
     * <p>
     * 调用逻辑：
     * 1. 将 sid 加入 Redis 黑名单
     * 2. 查询 sid 关联的所有系统
     * 3. 逐一调用各系统的 {@link SystemTokenRevokeRpc#revokeSession} 同步通知
     *    （失败只打印 error 日志，不影响主流程；后续可改为 MQ 异步通知）
     *
     * @param request sid、clientId、本地 jti 和退出原因
     */
    @RpcPublic
    ApiResponse<Void> logout(LogoutRpcRequest request);
}

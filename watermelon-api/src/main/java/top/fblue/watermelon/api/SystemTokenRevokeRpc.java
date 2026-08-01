package top.fblue.watermelon.api;

import top.fblue.common.annotation.RpcPublic;
import top.fblue.watermelon.api.request.TokenRevokeRequest;

/**
 * 其他系统实现的 sid 吊销 Dubbo RPC 接口
 * <p>
 * 系统A、系统B 需要：
 * 1. 引入 watermelon-api 作为依赖
 * 2. 实现本接口，使用 @DubboService 暴露服务
 * <p>
 * watermelon-auth 在退出登录时，会通过 @DubboReference 调用各系统的实现，
 * 通知其将 sid 加入本地黑名单。
 */
public interface SystemTokenRevokeRpc {

    /**
     * 吊销指定 sid（通知系统将其加入黑名单）
     *
     * @param request eventId、sid、sessionExpireAt、reason
     */
    @RpcPublic
    void revokeSession(TokenRevokeRequest request);
}

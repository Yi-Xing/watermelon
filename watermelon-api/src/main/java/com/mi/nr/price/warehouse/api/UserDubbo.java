package com.mi.nr.price.warehouse.api;

import com.mi.nr.price.warehouse.response.UserBaseResponse;

import java.util.List;

/**
 * 用户 Dubbo RPC 接口契约（纯接口，无框架依赖）
 * 实现位于 watermelon-service 的 dubbo 包，调用方需在 RpcContext 中传递秘钥（rpc-secret）鉴权
 */
public interface UserDubbo {

    /**
     * 查询用户基本信息
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    UserBaseResponse getUser(Long userId);

    /**
     * 批量查询用户基本信息
     *
     * @param userIdList 用户ID列表
     * @return 用户基本信息列表
     */
    List<UserBaseResponse> getUserList(List<Long> userIdList);
}

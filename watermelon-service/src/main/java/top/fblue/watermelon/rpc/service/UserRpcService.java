package top.fblue.watermelon.rpc.service;

import top.fblue.watermelon.api.response.UserBaseResponse;

import java.util.List;

/**
 * Dubbo 层用户服务，供 UserDubboImpl 调用，内部调用 domain.service
 */
public interface UserRpcService {

    /**
     * 根据ID获取用户基本信息
     */
    UserBaseResponse getUserBase(Long userId);

    /**
     * 批量获取用户基本信息
     */
    List<UserBaseResponse> getUserBaseList(List<Long> userIdList);
}

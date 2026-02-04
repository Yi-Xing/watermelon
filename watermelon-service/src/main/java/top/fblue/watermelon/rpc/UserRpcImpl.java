package top.fblue.watermelon.rpc;

import top.fblue.watermelon.api.UserRpc;
import top.fblue.watermelon.api.response.UserBaseResponse;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import top.fblue.watermelon.rpc.service.UserRpcService;
import top.fblue.common.response.ApiResponse;

import java.util.List;

/**
 * 用户 Dubbo RPC 接口实现
 */
@DubboService
public class UserRpcImpl implements UserRpc {

    @Resource
    private UserRpcService userRpcService;

    @Override
    public ApiResponse<UserBaseResponse> getUser(Long userId) {
        UserBaseResponse user = userRpcService.getUserBase(userId);
        return ApiResponse.success(user);
    }

    @Override
    public ApiResponse<List<UserBaseResponse>> getUserList(List<Long> userIdList) {
        List<UserBaseResponse> users = userRpcService.getUserBaseList(userIdList);
        return ApiResponse.success(users);
    }
}

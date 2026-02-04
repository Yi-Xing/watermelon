package top.fblue.watermelon.api;

import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.api.response.UserBaseResponse;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 用户 RPC 接口
 */
public interface UserRpc {

    /**
     * 查询用户基本信息
     *
     * @param userId 用户ID（不能为 null）
     * @return 用户基本信息
     */
    ApiResponse<UserBaseResponse> getUser(@NotNull(message = "用户ID不能为空") Long userId);

    /**
     * 批量查询用户基本信息
     *
     * @param userIdList 用户ID列表（不能为 null 或空）
     * @return 用户基本信息列表
     */
    ApiResponse<List<UserBaseResponse>> getUserList(@NotNull(message = "用户ID列表不能为空")
                                                    @NotEmpty(message = "用户ID列表不能为空")
                                                    List<Long> userIdList);
}

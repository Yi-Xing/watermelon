package top.fblue.watermelon.dubbo;

import com.mi.nr.price.warehouse.api.UserDubbo;
import com.mi.nr.price.warehouse.response.UserBaseResponse;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 Dubbo RPC 接口实现
 * 对外暴露 Dubbo 服务，调用方需在 RpcContext 中传递秘钥（rpc-secret）鉴权
 */
@DubboService
public class UserDubboImpl implements UserDubbo {

    @Resource
    private UserRepository userRepository;

    /**
     * 查询用户基本信息
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @Override
    public UserBaseResponse getUser(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }

        return convertToUserBaseResponse(user);
    }

    /**
     * 批量查询用户基本信息
     *
     * @param userIdList 用户ID列表
     * @return 用户基本信息列表
     */
    @Override
    public List<UserBaseResponse> getUserList(List<Long> userIdList) {
        if (userIdList == null || userIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userRepository.findByIds(userIdList);
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream()
                .map(this::convertToUserBaseResponse)
                .collect(Collectors.toList());
    }

    /**
     * 将 User 实体转换为 UserBaseResponse
     *
     * @param user 用户实体
     * @return 用户基本信息响应对象
     */
    private UserBaseResponse convertToUserBaseResponse(User user) {
        UserBaseResponse response = new UserBaseResponse();
        response.setId(user.getId());
        response.setName(user.getUsername());
        return response;
    }
}

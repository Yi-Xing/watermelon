package top.fblue.watermelon.rpc.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.api.response.UserBaseResponse;
import top.fblue.watermelon.domain.user.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dubbo 层用户转换器，负责 User 到 RPC 响应对象（如 UserBaseResponse）的转换
 */
@Component
public class UserRpcConverter {

    public UserBaseResponse toUserBaseResponse(User user) {
        if (user == null) {
            return null;
        }
        UserBaseResponse response = new UserBaseResponse();
        response.setId(user.getId());
        response.setName(user.getUsername());
        return response;
    }

    public List<UserBaseResponse> toUserBaseResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toUserBaseResponse)
                .collect(Collectors.toList());
    }
}

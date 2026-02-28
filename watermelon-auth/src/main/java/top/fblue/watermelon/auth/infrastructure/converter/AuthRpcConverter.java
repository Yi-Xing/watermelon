package top.fblue.watermelon.auth.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.api.response.UserBaseResponse;
import top.fblue.watermelon.auth.domain.user.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 RPC 与 Domain 实体转换器
 * UserBaseResponse -> User
 *
 * @author banana
 */
@Component
public class AuthRpcConverter {

    /**
     * 将 UserBaseResponse 转为领域 User
     */
    public User toDomain(UserBaseResponse u) {
        if (u == null) {
            return null;
        }
        return User.builder()
                .id(u.getId())
                .username(u.getName())
                .build();
    }

    /**
     * 批量将 UserBaseResponse 转为领域 User 列表
     */
    public List<User> toDomainList(List<UserBaseResponse> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}

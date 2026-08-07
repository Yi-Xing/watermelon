package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户持久化对象与 SSO 最小用户对象转换器。
 */
@Component
public class SsoUserPOConverter {

    /**
     * 将用户持久化对象转换为 SSO 流程所需的最小用户对象。
     *
     * @param userPO 用户持久化对象
     * @return SSO 最小用户对象；入参为空时返回 {@code null}
     */
    public User toSsoUser(UserPO userPO) {
        if (userPO == null) {
            return null;
        }
        return User.builder()
                .id(userPO.getId())
                .username(userPO.getName())
                .build();
    }
}

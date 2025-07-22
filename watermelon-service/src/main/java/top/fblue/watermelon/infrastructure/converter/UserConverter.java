package top.fblue.watermelon.infrastructure.converter;

import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.infrastructure.po.UserPO;

public class UserConverter {

    public User toDomain(UserPO po) {
        if (po == null) return null;
        return new User();
    }

    public UserPO toPO(User domain) {
        if (domain == null) return null;
        return new UserPO();
    }
}

package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户数据转换器
 */
@Component
public class UserConverter {

    /**
     * PO转Domain
     */
    public User toDomain(UserPO po) {
        if (po == null) return null;
        return User.builder()
                .id(po.getId())
                .username(po.getName())
                .email(po.getEmail())
                .phone(po.getPhone())
                .password(po.getPassword())
                .state(po.getState())
                .remark(po.getRemark())
                .build();
    }

    /**
     * Domain转PO
     */
    public UserPO toPO(User domain) {
        if (domain == null) return null;
        return UserPO.builder()
                .id(domain.getId())
                .name(domain.getUsername())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .password(domain.getPassword())
                .state(domain.getState())
                .remark(domain.getRemark())
                .build();
    }
}

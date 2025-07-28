package top.fblue.watermelon.infrastructure.repository.user.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户转换器
 * 负责Domain和Infrastructure层之间的数据转换
 */
@Component
public class UserPOConverter {

    /**
     * PO转换为Domain实体
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
                .createdBy(po.getCreatedBy())
                .createdTime(po.getCreatedTime())
                .updatedBy(po.getUpdatedBy())
                .updatedTime(po.getUpdatedTime())
                .build();
    }

    /**
     * Domain实体转换为PO
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

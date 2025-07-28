package top.fblue.watermelon.infrastructure.repository.resource.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.domain.resource.entity.UserBasicInfo;
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
    public UserBasicInfo toDomain(UserPO po) {
        if (po == null) return null;
        
        return UserBasicInfo.builder()
                .id(po.getId())
                .username(po.getName())
                .build();
    }
}

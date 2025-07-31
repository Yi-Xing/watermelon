package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.infrastructure.po.UserRolePO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关系转换器
 * 负责Domain和Infrastructure层之间的数据转换
 */
@Component
public class UserRolePOConverter {

    /**
     * 转换为POList
     */
    public List<UserRolePO> toPOList(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return roleIds.stream()
                .map(roleId -> UserRolePO.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build())
                .collect(Collectors.toList());
    }
} 
package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.infrastructure.po.RoleResourceNodePO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色资源关系转换器
 * 负责Domain和Infrastructure层之间的数据转换
 */
@Component
public class RoleResourceNodePOConverter {

    /**
     * 转换为POList
     */
    public List<RoleResourceNodePO> toPOList(Long roleId, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return new ArrayList<>();
        }
        return resourceIds.stream()
                .map(resourceId -> RoleResourceNodePO.builder()
                        .roleId(roleId)
                        .resourceNodeId(resourceId)
                        .build())
                .collect(Collectors.toList());
    }
} 
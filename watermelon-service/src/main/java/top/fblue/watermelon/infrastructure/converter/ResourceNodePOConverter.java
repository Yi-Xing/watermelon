package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

/**
 * 资源转换器
 * 负责Domain和Infrastructure层之间的数据转换
 */
@Component
public class ResourceNodePOConverter {

    /**
     * PO转换为Domain实体
     */
    public ResourceNode toDomain(ResourceNodePO po) {
        if (po == null) return null;
        
        return ResourceNode.builder()
                .id(po.getId())
                .name(po.getName())
                .type(po.getType())
                .code(po.getCode())
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
    public ResourceNodePO toPO(ResourceNode domain) {
        if (domain == null) return null;
        return ResourceNodePO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .type(domain.getType())
                .code(domain.getCode())
                .state(domain.getState())
                .remark(domain.getRemark())
                .build();
    }
} 
package top.fblue.watermelon.infrastructure.converter;

import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.infrastructure.po.ResourceRelationPO;

/**
 * 资源关系PO转换器
 */
public class ResourceRelationPOConverter {
    
    /**
     * PO转Domain
     */
    public static ResourceRelation toDomain(ResourceRelationPO po) {
        if (po == null) {
            return null;
        }
        
        return ResourceRelation.builder()
                .id(po.getId())
                .parentId(po.getParentId())
                .childId(po.getChildId())
                .orderNum(po.getOrderNum())
                .createdBy(po.getCreatedBy())
                .createdTime(po.getCreatedTime())
                .updatedBy(po.getUpdatedBy())
                .updatedTime(po.getUpdatedTime())
                .build();
    }
    
    /**
     * Domain转PO
     */
    public static ResourceRelationPO toPO(ResourceRelation entity) {
        if (entity == null) {
            return null;
        }
        
        return ResourceRelationPO.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .childId(entity.getChildId())
                .orderNum(entity.getOrderNum())
                .createdBy(entity.getCreatedBy())
                .createdTime(entity.getCreatedTime())
                .updatedBy(entity.getUpdatedBy())
                .updatedTime(entity.getUpdatedTime())
                .build();
    }
}

package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.infrastructure.po.ResourcePO;

/**
 * 资源转换器
 * 负责Domain和Infrastructure层之间的数据转换
 * 注意：当前resource domain实体类尚未创建，此converter为基础框架
 */
@Component
public class ResourcePOConverter {

    /**
     * PO转换为Domain实体
     * 
     * 注意：需要先创建对应的Resource domain实体类
     * public Resource toDomain(ResourcePO po) {
     *     if (po == null) return null;
     *     
     *     return Resource.builder()
     *             .id(po.getId())
     *             .name(po.getName())
     *             .type(po.getType())
     *             .code(po.getCode())
     *             .orderNum(po.getOrderNum())
     *             .parentId(po.getParentId())
     *             .state(po.getState())
     *             .remark(po.getRemark())
     *             .createdBy(po.getCreatedBy())
     *             .createdTime(po.getCreatedTime())
     *             .updatedBy(po.getUpdatedBy())
     *             .updatedTime(po.getUpdatedTime())
     *             .build();
     * }
     */

    /**
     * Domain实体转换为PO
     * 
     * 注意：需要先创建对应的Resource domain实体类
     * public ResourcePO toPO(Resource domain) {
     *     if (domain == null) return null;
     *     return ResourcePO.builder()
     *             .id(domain.getId())
     *             .name(domain.getName())
     *             .type(domain.getType())
     *             .code(domain.getCode())
     *             .orderNum(domain.getOrderNum())
     *             .parentId(domain.getParentId())
     *             .state(domain.getState())
     *             .remark(domain.getRemark())
     *             .build();
     * }
     */
} 
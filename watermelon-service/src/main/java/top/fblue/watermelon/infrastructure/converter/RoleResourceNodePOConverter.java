package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;

/**
 * 角色资源关系转换器
 * 负责Domain和Infrastructure层之间的数据转换
 */
@Component
public class RoleResourceNodePOConverter {

    /**
     * PO转换为Domain实体
     * 
     * 注意：需要先创建对应的RoleResource domain实体类
     * public RoleResource toDomain(RoleResourcePO po) {
     *     if (po == null) return null;
     *     
     *     return RoleResource.builder()
     *             .id(po.getId())
     *             .roleId(po.getRoleId())
     *             .resourceId(po.getResourceId())
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
     * 注意：需要先创建对应的RoleResource domain实体类
     * public RoleResourcePO toPO(RoleResource domain) {
     *     if (domain == null) return null;
     *     return RoleResourcePO.builder()
     *             .id(domain.getId())
     *             .roleId(domain.getRoleId())
     *             .resourceId(domain.getResourceId())
     *             .build();
     * }
     */
} 
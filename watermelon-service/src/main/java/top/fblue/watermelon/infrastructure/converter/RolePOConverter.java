package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.infrastructure.po.RolePO;

/**
 * 角色转换器
 * 负责Domain和Infrastructure层之间的数据转换
 * 注意：当前role domain实体类尚未创建，此converter为基础框架
 */
@Component
public class RolePOConverter {

    /**
     * PO转换为Domain实体
     * 
     * 注意：需要先创建对应的Role domain实体类
     * public Role toDomain(RolePO po) {
     *     if (po == null) return null;
     *     
     *     return Role.builder()
     *             .id(po.getId())
     *             .name(po.getName())
     *             .orderNum(po.getOrderNum())
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
     * 注意：需要先创建对应的Role domain实体类
     * public RolePO toPO(Role domain) {
     *     if (domain == null) return null;
     *     return RolePO.builder()
     *             .id(domain.getId())
     *             .name(domain.getName())
     *             .orderNum(domain.getOrderNum())
     *             .state(domain.getState())
     *             .remark(domain.getRemark())
     *             .build();
     * }
     */
} 
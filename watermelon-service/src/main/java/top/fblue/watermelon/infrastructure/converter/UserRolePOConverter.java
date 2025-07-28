package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.infrastructure.po.UserRolePO;

/**
 * 用户角色关系转换器
 * 负责Domain和Infrastructure层之间的数据转换
 */
@Component
public class UserRolePOConverter {

    /**
     * PO转换为Domain实体
     * 
     * 注意：需要先创建对应的UserRole domain实体类
     * public UserRole toDomain(UserRolePO po) {
     *     if (po == null) return null;
     *     
     *     return UserRole.builder()
     *             .id(po.getId())
     *             .userId(po.getUserId())
     *             .roleId(po.getRoleId())
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
     * 注意：需要先创建对应的UserRole domain实体类
     * public UserRolePO toPO(UserRole domain) {
     *     if (domain == null) return null;
     *     return UserRolePO.builder()
     *             .id(domain.getId())
     *             .userId(domain.getUserId())
     *             .roleId(domain.getRoleId())
     *             .build();
     * }
     */
} 
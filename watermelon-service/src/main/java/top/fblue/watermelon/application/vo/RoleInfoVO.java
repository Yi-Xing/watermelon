package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 角色信息视图对象
 */
@Data
@Builder
public class RoleInfoVO {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 状态：1 启用 2 禁用
     */
    private Integer state;
    
    /**
     * 状态描述
     */
    private String stateDesc;
} 
package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 角色视图对象
 */
@Data
@Builder
public class RoleVO {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 状态：1 启用 2 禁用
     */
    private Integer state;
    
    /**
     * 状态描述
     */
    private String stateDesc;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建人信息
     */
    private UserInfoVO createdBy;
    
    /**
     * 创建时间
     */
    private String createdTime;
    
    /**
     * 更新人信息
     */
    private UserInfoVO updatedBy;
    
    /**
     * 更新时间
     */
    private String updatedTime;
} 
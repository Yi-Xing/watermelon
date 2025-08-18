package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 资源视图对象
 */
@Data
@Builder
public class ResourceNodeVO {
    
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源类型：1 页面，2 按钮，3 接口
     */
    private Integer type;
    
    /**
     * 资源类型描述
     */
    private String typeDesc;
    
    /**
     * 资源code
     */
    private String code;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 父级ID
     */
    private Long parentId;
    
    /**
     * 父级资源名称
     */
    private String parentName;
    
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
    private UserBaseVO createdBy;
    
    /**
     * 创建时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createdTime;
    
    /**
     * 更新人信息
     */
    private UserBaseVO updatedBy;
    
    /**
     * 更新时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String updatedTime;
} 
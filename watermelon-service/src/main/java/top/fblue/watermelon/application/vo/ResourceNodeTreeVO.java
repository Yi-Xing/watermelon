package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 资源树形结构视图对象
 */
@Data
@Builder
public class ResourceNodeTreeVO {
    
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
    
    /**
     * 子节点列表
     */
    private List<ResourceNodeTreeVO> children;
} 
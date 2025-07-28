package top.fblue.watermelon.domain.resource.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import top.fblue.watermelon.domain.resource.entity.UserBasicInfo;

import java.time.LocalDateTime;

/**
 * 资源节点领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceNode {
    
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源类型：1页面 2按钮 3接口
     */
    private Integer type;
    
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
     * 备注
     */
    private String remark;
    
    /**
     * 创建人
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    private Long updatedBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 父级资源节点
     */
    private ResourceNode parentNode;
    // 关联的用户信息（可选，用于展示时的详细信息）
    private UserBasicInfo createdByUser;
    private UserBasicInfo updatedByUser;
} 
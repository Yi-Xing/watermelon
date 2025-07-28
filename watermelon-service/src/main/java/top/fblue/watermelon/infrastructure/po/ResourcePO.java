package top.fblue.watermelon.infrastructure.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 资源持久化对象
 * 对应数据库表 resource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("resource")
public class ResourcePO {
    
    /**
     * 资源ID
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /**
     * 是否删除：0 未删除 1 已删除
     */
    @TableLogic
    private Integer isDeleted;
} 
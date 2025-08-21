package top.fblue.watermelon.infrastructure.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 资源关系持久化对象
 * 对应数据库表 resource_relation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("resource_relation")
public class ResourceRelationPO {
    
    /**
     * 关系ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 父级资源ID
     */
    private Long parentId;
    
    /**
     * 子级资源ID
     */
    private Long childId;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
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

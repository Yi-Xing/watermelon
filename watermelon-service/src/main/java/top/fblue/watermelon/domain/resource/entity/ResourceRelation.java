package top.fblue.watermelon.domain.resource.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 资源关系领域实体
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRelation {
    
    /**
     * 关系ID
     */
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
}

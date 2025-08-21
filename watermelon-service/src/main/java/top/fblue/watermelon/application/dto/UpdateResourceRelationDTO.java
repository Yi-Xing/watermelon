package top.fblue.watermelon.application.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 更新资源关联DTO
 */
@Data
public class UpdateResourceRelationDTO {
    
    /**
     * 资源关联ID
     */
    @NotNull(message = "资源关联ID不能为空")
    private Long id;
    
    /**
     * 父级资源ID（可为空，表示顶级资源）
     */
    private Long parentId;
    
    /**
     * 子级资源ID
     */
    @NotNull(message = "子级资源ID不能为空")
    private Long childId;
    
    /**
     * 显示顺序 - 必填
     */
    @NotNull(message = "显示顺序不能为空")
    @Min(value = 0, message = "显示顺序必须大于等于0")
    private Integer orderNum;
}

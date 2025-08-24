package top.fblue.watermelon.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 资源关联导入DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRelationImportDTO {

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
}

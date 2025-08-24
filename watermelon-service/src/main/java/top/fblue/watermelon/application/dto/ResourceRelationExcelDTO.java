package top.fblue.watermelon.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 资源关联ExcelDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRelationExcelDTO {

    /**
     * 资源层级路径（从第一列到最后一列的资源信息）
     */
    private List<String> resourcePath;
    /**
     * 显示顺序
     */
    private Integer orderNum;
}

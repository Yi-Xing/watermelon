package top.fblue.watermelon.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Excel导入结果VO（通用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultVO {

    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 导入总数
     */
    private Integer totalCount;
    
    /**
     * 新增数量
     */
    private Integer addedCount;
    
    /**
     * 更新数量
     */
    private Integer updatedCount;
    
    /**
     * 删除数量
     */
    private Integer deletedCount;
    
    /**
     * 错误信息列表
     */
    private List<String> errors;
}

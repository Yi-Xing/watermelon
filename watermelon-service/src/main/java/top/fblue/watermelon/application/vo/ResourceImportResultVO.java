package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

import java.util.*;

/**
 * 导入结果
 */
@Data
@Builder
public class ResourceImportResultVO {
    /**
     * 总行数
     */
    private int totalRows;
    
    /**
     * 插入数量
     */
    private int insertedRows;
    
    /**
     * 更新数量
     */
    private int updatedRows;
    
    /**
     * 删除数量
     */
    private int deletedRows;
    
    /**
     * 错误信息列表
     */
    private List<String> errors;
    
    /**
     * 是否成功
     */
    private boolean success;
    
}
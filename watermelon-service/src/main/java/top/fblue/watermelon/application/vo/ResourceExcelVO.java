package top.fblue.watermelon.application.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 资源Excel导出VO
 */
@Data
public class ResourceExcelVO {
    
    /**
     * 上级资源code
     */
    @ExcelProperty("上级资源")
    private String parentCode;
    
    /**
     * 资源名称
     */
    @ExcelProperty("名称")
    private String name;
    
    /**
     * 资源code
     */
    @ExcelProperty("code")
    private String code;
    
    /**
     * 资源类型
     */
    @ExcelProperty("类型")
    private String type;
    
    /**
     * 显示顺序
     */
    @ExcelProperty("显示顺序")
    private Integer orderNum;
    
    /**
     * 状态
     */
    @ExcelProperty("状态")
    private String state;
    
    /**
     * 备注
     */
    @ExcelProperty("备注")
    private String remark;
} 
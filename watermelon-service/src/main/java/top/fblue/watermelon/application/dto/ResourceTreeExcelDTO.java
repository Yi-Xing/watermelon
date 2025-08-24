package top.fblue.watermelon.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源关系树Excel导出VO
 * 用于动态列生成的空VO类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceTreeExcelDTO {

    /**
     * 动态列映射 - 资源信息所在的列数
     */
    private Integer column;

    /**
     * 资源信息：资源名称/资源code
     */
    private String resourceInfo;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 资源状态
     */
    private String state;
}

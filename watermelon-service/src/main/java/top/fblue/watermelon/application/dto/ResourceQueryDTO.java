package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 资源查询DTO
 */
@Data
public class ResourceQueryDTO {
    
    /**
     * 资源名称（模糊查询）
     */
    private String name;
    
    /**
     * 状态：1 启用 2 禁用
     */
    @Min(value = 1, message = "状态值不正确")
    @Max(value = 2, message = "状态值不正确")
    private Integer state;
} 
package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 角色查询DTO
 */
@Data
public class RoleQueryDTO {
    
    /**
     * 角色名称（模糊查询）
     */
    private String name;
    
    /**
     * 状态：1 启用 2 禁用
     */
    @Min(value = 1, message = "状态值不正确")
    @Max(value = 2, message = "状态值不正确")
    private Integer state;
    
    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    @NotNull(message = "页码不能为空")
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能大于100")
    @NotNull(message = "每页大小不能为空")
    private Integer pageSize = 20;
} 
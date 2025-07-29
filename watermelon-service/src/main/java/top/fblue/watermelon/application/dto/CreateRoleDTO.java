package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建角色DTO
 */
@Data
public class CreateRoleDTO {
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 3, max = 10, message = "角色名称长度必须在3-10个字符之间")
    private String name;
    
    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Min(value = 0, message = "显示顺序必须大于等于0")
    private Integer orderNum;
    
    /**
     * 状态：1 启用 2 禁用
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值不正确")
    @Max(value = 2, message = "状态值不正确")
    private Integer state;
    
    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
} 
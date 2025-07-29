package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新资源DTO
 */
@Data
public class UpdateResourceDTO {
    
    /**
     * 资源ID
     */
    @NotNull(message = "资源ID不能为空")
    private Long id;
    
    /**
     * 上级资源ID
     */
    private Long parentId;
    
    /**
     * 资源名称
     */
    @NotBlank(message = "资源名称不能为空")
    @Size(min = 3, max = 10, message = "资源名称长度必须在3-10个字符之间")
    private String name;
    
    /**
     * 资源类型：1 页面，2 按钮，3 接口
     */
    @NotNull(message = "资源类型不能为空")
    @Min(value = 1, message = "资源类型值不正确")
    @Max(value = 3, message = "资源类型值不正确")
    private Integer type;
    
    /**
     * 资源code
     */
    @NotBlank(message = "资源code不能为空")
    @Size(min = 1, max = 100, message = "资源code长度必须在1-100个字符之间")
    private String code;
    
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
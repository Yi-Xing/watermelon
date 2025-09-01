package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;
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
     * 资源名称 - 必填，2~10个字符，不能包含"/"
     */
    @NotBlank(message = "资源名称不能为空")
    @Size(min = 2, max = 10, message = "资源名称长度必须在2~10个字符之间")
    @Pattern(regexp = "^[^/]*$", message = "资源名称不能包含'/'字符")
    private String name;
    
    /**
     * 资源类型：1 页面，2 按钮，3 接口，4 目录
     */
    @NotNull(message = "资源类型不能为空")
    @Min(value = 1, message = "资源类型值不正确")
    @Max(value = 4, message = "资源类型值不正确")
    private Integer type;
    
    /**
     * 资源code
     */
    @NotBlank(message = "资源code不能为空")
    @Size(min = 1, max = 100, message = "资源code长度必须在1-100个字符之间")
    private String code;
    
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
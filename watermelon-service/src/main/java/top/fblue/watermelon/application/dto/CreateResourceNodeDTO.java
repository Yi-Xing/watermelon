package top.fblue.watermelon.application.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 创建资源DTO
 */
@Data
public class CreateResourceNodeDTO {
    
    /**
     * 上级资源ID - 非必填
     */
    private Long parentId;
    
    /**
     * 资源名称 - 必填，3~10个字符
     */
    @NotBlank(message = "资源名称不能为空")
    @Size(min = 3, max = 10, message = "资源名称长度必须在3~10个字符之间")
    private String name;
    
    /**
     * 资源类型 - 必填，1 页面，2 按钮，3 接口
     */
    @NotNull(message = "资源类型不能为空")
    @Min(value = 1, message = "资源类型值不能小于1")
    @Max(value = 3, message = "资源类型值不能大于3")
    private Integer type;
    
    /**
     * 资源code - 必填，唯一标识，1~100 字符
     */
    @NotBlank(message = "资源code不能为空")
    @Size(min = 1, max = 100, message = "资源code长度必须在1~100个字符之间")
    private String code;
    
    /**
     * 显示顺序 - 必填
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;
    
    /**
     * 状态 - 必填，1 启用、2 禁用
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值不能小于1")
    @Max(value = 2, message = "状态值不能大于2")
    private Integer state;
    
    /**
     * 备注 - 非必填，0~500 字符
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
} 
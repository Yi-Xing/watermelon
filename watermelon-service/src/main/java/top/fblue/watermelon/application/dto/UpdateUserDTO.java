package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/**
 * 更新用户DTO
 */
@Data
public class UpdateUserDTO {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;
    
    /**
     * 用户名称
     */
    @NotBlank(message = "用户名称不能为空")
    @Size(min = 2, max = 10, message = "用户名称长度必须在2~10个字符之间")
    private String name;

    /**
     * 邮箱 - 非必填，正则校验
     */
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号 - 非必填，正则校验
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
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
    
    /**
     * 角色ID列表
     */
    @NotNull(message = "角色列表不能为空")
    private List<Long> roleIds;
} 
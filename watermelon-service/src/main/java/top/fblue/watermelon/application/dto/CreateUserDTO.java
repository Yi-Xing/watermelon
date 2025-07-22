package top.fblue.watermelon.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 创建用户DTO
 */
@Data
public class CreateUserDTO {
    
    /**
     * 用户名称
     */
    @NotBlank(message = "用户名称不能为空")
    private String name;
    
    /**
     * 邮箱 - 可以为空，不为空时验证格式
     */
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号 - 可以为空，不为空时验证格式
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 状态
     */
    private Boolean active;
    
    /**
     * 备注
     */
    private String remark;
} 
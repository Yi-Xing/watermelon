package top.fblue.watermelon.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录DTO
 */
@Data
public class LoginDTO {
    
    /**
     * 手机号/邮箱
     */
    @NotBlank(message = "手机号/邮箱不能为空")
    private String account;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
} 
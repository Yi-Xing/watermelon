package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 重设密码DTO
 */
@Data
public class ResetPasswordDTO {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;
    
    /**
     * 新密码
     */
    @Size(min = 8, max = 8, message = "密码必须是8位数字")
    @Pattern(regexp = "^\\d{8}$", message = "密码必须是8位数字")
    private String password;
} 
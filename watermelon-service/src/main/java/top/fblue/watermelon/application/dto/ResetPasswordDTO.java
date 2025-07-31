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
    @NotBlank(message = "密码不能为空")
    private String password;
} 
package top.fblue.watermelon.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * 创建用户DTO
 */
@Data
public class CreateUserDTO {
    
    /**
     * 用户名称 - 必填，3~10个字符
     */
    @NotBlank(message = "用户名称不能为空")
    @Size(min = 3, max = 10, message = "用户名称长度必须在3~10个字符之间")
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
     * 密码 - 非必填，8位数
     */
    private String password;
    
    /**
     * 状态 - 启用(1)、禁用(2)
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值不能小于1")
    @Max(value = 2, message = "状态值不能大于2")
    private Integer state;
    
    /**
     * 备注 - 0~500字符
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
    
    /**
     * 角色列表 - 可选多个
     */
    @NotNull(message = "角色列表不能为空")
    private List<Long> roleIds;
} 
package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户视图对象
 */
@Data
@Builder
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名称
     */
    private String name;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态：1 启用 2 禁用
     */
    private Integer state;
    
    /**
     * 状态描述
     */
    private String stateDesc;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建人信息
     */
    private UserInfoVO createdBy;
    
    /**
     * 创建时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createdTime;
    
    /**
     * 更新人信息
     */
    private UserInfoVO updatedBy;
    
    /**
     * 更新时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String updatedTime;
}

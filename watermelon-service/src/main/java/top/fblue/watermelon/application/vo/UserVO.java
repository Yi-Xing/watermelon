package top.fblue.watermelon.application.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
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
     * 创建人
     */
    private Integer createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    private Integer updatedBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

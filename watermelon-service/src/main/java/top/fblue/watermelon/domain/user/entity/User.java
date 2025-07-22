package top.fblue.watermelon.domain.user.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户聚合根
 */
@Getter
@Setter
public class User {
    
    private Long id;
    private String username;
    private Email email;
    private String phone;
    private String password;
    private boolean active;  // 业务状态：是否激活
    private String remark;

    public User() {
    }
    
    // 领域行为 - 使用active字段
    public void changeEmail(Email newEmail) {
        this.email = newEmail;
    }
    
    public void deactivate() {
        this.active = false;  // 业务状态：停用
    }
    

    
    /**
     * 检查用户是否可以登录
     */
    public boolean canLogin() {
        return active && state == 1;
    }
    
    /**
     * 检查用户是否可以接收邮件
     */
    public boolean canReceiveEmail() {
        return active;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        if (!active) return "已停用";
        if (state == 1) return "正常";
        if (state == 2) return "已禁用";
        return "未知状态";
    }
}

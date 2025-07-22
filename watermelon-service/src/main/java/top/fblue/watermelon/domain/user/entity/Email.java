package top.fblue.watermelon.domain.user.entity;

import lombok.Value;

/**
 * 邮箱值对象
 * 体现了DDD中值对象的设计原则
 */
@Value
public class Email {
    String value;
    
    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        
        if (!isValidEmail(value)) {
            throw new IllegalArgumentException("邮箱格式不正确: " + value);
        }
        
        this.value = value.trim().toLowerCase();
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        // 简单的邮箱格式验证
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * 获取邮箱域名
     */
    public String getDomain() {
        return value.substring(value.indexOf("@") + 1);
    }
    
    /**
     * 获取邮箱用户名部分
     */
    public String getUsername() {
        return value.substring(0, value.indexOf("@"));
    }
    
    /**
     * 检查是否为特定域名
     */
    public boolean isFromDomain(String domain) {
        return getDomain().equalsIgnoreCase(domain);
    }
    
    /**
     * 检查是否为常见邮箱服务商
     */
    public boolean isCommonProvider() {
        String domain = getDomain().toLowerCase();
        return domain.equals("gmail.com") || 
               domain.equals("qq.com") || 
               domain.equals("163.com") || 
               domain.equals("126.com");
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email email = (Email) obj;
        return value.equals(email.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

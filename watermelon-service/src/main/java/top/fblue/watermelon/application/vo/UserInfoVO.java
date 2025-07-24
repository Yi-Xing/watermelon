package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息视图对象
 * 用于表示创建人、更新人等用户信息
 */
@Data
@Builder
public class UserInfoVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名称
     */
    private String name;
} 
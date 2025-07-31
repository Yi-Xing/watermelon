package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应VO
 */
@Data
@Builder
public class LoginVO {
    
    /**
     * 用户信息
     */
    private UserVO userInfo;
    
    /**
     * 访问令牌
     */
    private String token;
    
}
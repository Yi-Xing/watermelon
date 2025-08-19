package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.domain.user.entity.UserToken;

/**
 * 登录应用服务接口
 */
public interface AuthApplicationService {
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 退出登录
     */
    void logout(String authHeader);
    
    /**
     * 刷新token
     */
    String refreshToken(String authHeader);

    /**
     * 获取当前登录用户信息
     */
    CurrentUserVO getCurrentUser();

    /**
     * 验证token有效性并获取 UserToken
     */
    UserToken validateToken(String token);

    /**
     * 判断当前登录用户是否有指定接口资源的code权限
     */
    boolean hasPermission(String resourceCode);
} 
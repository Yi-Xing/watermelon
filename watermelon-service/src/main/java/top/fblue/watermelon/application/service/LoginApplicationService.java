package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.vo.LoginVO;

/**
 * 登录应用服务接口
 */
public interface LoginApplicationService {
    
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
} 
package top.fblue.watermelon.auth.application.service;


import top.fblue.watermelon.auth.common.dto.UserDTO;

/**
 * 鉴权服务接口
 */
public interface AuthCommonApplicationService {

    /**
     * 退出登录
     */
    void logout(String authHeader);

    /**
     * 刷新token
     */
    String refreshToken(String authHeader);

    /**
     * 验证token有效性并获取 UserDTO
     */
    UserDTO validateToken(String token);
}
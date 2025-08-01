package top.fblue.watermelon.domain.user.service;

import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserToken;

/**
 * Token服务接口
 * 抽象token管理逻辑，支持多种存储方式（Map、Redis等）
 */
public interface TokenDomainService {
    
    /**
     * 生成并存储用户token
     */
    String generateToken(User user);
    
    /**
     * 验证token有效性
     */
    UserToken validateToken(String token);
    
    /**
     * 使token失效
     */
    void invalidateToken(String token);
    
    /**
     * 刷新token
     */
    String refreshToken(String token);
}
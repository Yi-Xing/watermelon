package top.fblue.watermelon.domain.user.repository;

import top.fblue.watermelon.domain.user.entity.UserToken;

/**
 * 用户Token仓储接口
 */
public interface UserTokenRepository {

    /**
     * 创建Token
     */
    String create(Long userId);
    
    /**
     * 根据Token查找
     */
    UserToken findByToken(String token);

    /**
     * 删除Token
     */
    void deleteToken(String token);
    
    /**
     * 删除过期Token
     */
    void deleteExpiredTokens();
}
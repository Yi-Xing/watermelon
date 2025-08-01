package top.fblue.watermelon.domain.user.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;
import top.fblue.watermelon.domain.user.service.TokenDomainService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token服务实现类
 * 使用Repository模式，支持多种存储方式
 */
@Slf4j
@Service
public class TokenDomainServiceImpl implements TokenDomainService {

    @Resource
    private UserTokenRepository userTokenRepository;

    /**
     * Token过期时间（天）
     */
    private static final int TOKEN_EXPIRE_DAY = 7;

    @Override
    public String generateToken(User user) {
        // 生成唯一token
        String token = UUID.randomUUID().toString();

        // 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusDays(TOKEN_EXPIRE_DAY);

        // 创建UserToken实体
        UserToken userToken = UserToken.builder()
                .userId(user.getId())
                .token(token)
                .expireTime(expireTime)
                .createdTime(LocalDateTime.now())
                .build();

        // 通过Repository保存
        userTokenRepository.save(userToken);

        return token;
    }

    @Override
    public UserToken validateToken(String token) {
        UserToken userToken = userTokenRepository.findByToken(token);
        if (userToken == null) {
            throw new IllegalArgumentException("Token无效");
        }

        // 检查token是否过期
        if (LocalDateTime.now().isAfter(userToken.getExpireTime())) {
            // 清理过期token
            userTokenRepository.deleteExpiredTokens();
            throw new IllegalArgumentException("Token已过期");
        }

        return userToken;
    }

    @Override
    public void invalidateToken(String token) {
        userTokenRepository.deleteToken(token);
    }

    @Override
    public String refreshToken(String token) {
        UserToken userToken = userTokenRepository.findByToken(token);
        if (userToken == null) {
            throw new IllegalArgumentException("Token无效");
        }

        // 生成新token
        String newToken = UUID.randomUUID().toString();
        LocalDateTime newExpireTime = LocalDateTime.now().plusHours(TOKEN_EXPIRE_DAY);

        UserToken newUserToken = UserToken.builder()
                .userId(userToken.getUserId())
                .token(newToken)
                .expireTime(newExpireTime)
                .createdTime(LocalDateTime.now())
                .build();

        // 通过Repository保存新token
        userTokenRepository.save(newUserToken);

        // 删除旧token
        userTokenRepository.deleteToken(token);

        return newToken;
    }

    @Override
    public Long getUserIdByToken(String token) {
        UserToken userToken = userTokenRepository.findByToken(token);
        return userToken != null ? userToken.getUserId() : null;
    }
} 
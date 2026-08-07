package top.fblue.watermelon.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;
import top.fblue.watermelon.domain.user.service.TokenDomainService;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Token服务实现类
 * 使用Repository模式，支持多种存储方式
 */
@Service
@RequiredArgsConstructor
public class TokenDomainServiceImpl implements TokenDomainService {

    /** 用户 Token 仓储。 */
    private final UserTokenRepository userTokenRepository;

    /** Water 提供的应用统一时钟。 */
    private final Clock clock;

    @Override
    public String generateToken(User user) {
        return userTokenRepository.create(user.getId());
    }

    @Override
    public UserToken validateToken(String token) {
        UserToken userToken = userTokenRepository.findByToken(token);
        if (userToken == null) {
            throw new BusinessException("Token无效");
        }

        // 检查token是否过期
        if (LocalDateTime.now(clock).isAfter(userToken.getExpireTime())) {
            // 清理过期token
            userTokenRepository.deleteExpiredTokens();
            throw new BusinessException("Token已过期");
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
            throw new BusinessException("Token无效");
        }

        // 删除旧 token
        userTokenRepository.deleteToken(token);

        // 创建新 token
        return userTokenRepository.create(userToken.getUserId());
    }
}

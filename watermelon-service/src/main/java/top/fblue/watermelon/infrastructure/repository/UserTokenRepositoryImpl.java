package top.fblue.watermelon.infrastructure.repository;

import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户Token仓储实现类
 * 使用ConcurrentHashMap存储，后期需要用 Redis，可以创建 UserTokenRedisRepositoryImpl
 */
public class UserTokenRepositoryImpl implements UserTokenRepository {

    /**
     * Token过期时间（天）
     */
    private static final int TOKEN_EXPIRE_DAY = 7;

    /** Water 提供的应用统一时钟。 */
    private final Clock clock;

    /**
     * Token存储：token -> UserToken
     */
    private final Map<String, UserToken> TOKEN_STORE = new ConcurrentHashMap<>();

    /**
     * 创建基于内存的 Token 仓储。
     *
     * @param clock 应用统一时钟
     */
    public UserTokenRepositoryImpl(Clock clock) {
        this.clock = clock;
    }

    /**
     * 创建Token
     */
    @Override
    public String create(Long userId) {
        // 生成唯一token
        String token = UUID.randomUUID().toString();

        // 计算创建时间和过期时间
        LocalDateTime createdTime = LocalDateTime.now(clock);
        LocalDateTime expireTime = createdTime.plusDays(TOKEN_EXPIRE_DAY);

        // 创建UserToken实体
        UserToken userToken = UserToken.builder()
                .userId(userId)
                .token(token)
                .expireTime(expireTime)
                .createdTime(createdTime)
                .build();

        // 存储token
        TOKEN_STORE.put(userToken.getToken(), userToken);
        return token;
    }

    @Override
    public UserToken findByToken(String token) {
        return TOKEN_STORE.get(token);
    }

    @Override
    public void deleteToken(String token) {
        UserToken userToken = TOKEN_STORE.get(token);
        if (userToken == null) {
            return;
        }

        // 直接删除token
        TOKEN_STORE.remove(token);
    }

    @Override
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now(clock);

        // 遍历所有token，删除过期的
        TOKEN_STORE.entrySet().removeIf(entry -> {
            UserToken userToken = entry.getValue();
            return now.isAfter(userToken.getExpireTime());
        });
    }
}

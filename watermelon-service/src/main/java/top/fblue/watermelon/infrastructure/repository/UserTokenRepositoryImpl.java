package top.fblue.watermelon.infrastructure.repository;

import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.repository.UserTokenRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户Token仓储实现类
 * 使用ConcurrentHashMap存储，支持后期迁移到Redis
 */
@Repository
public class UserTokenRepositoryImpl implements UserTokenRepository {

    /**
     * Token存储：token -> UserToken
     */
    private static final Map<String, UserToken> TOKEN_STORE = new ConcurrentHashMap<>();

    /**
     * 用户Token映射：userId -> token
     */
    private static final Map<Long, String> USER_TOKEN_MAP = new ConcurrentHashMap<>();

    @Override
    public void save(UserToken userToken) {
        TOKEN_STORE.put(userToken.getToken(), userToken);
        USER_TOKEN_MAP.put(userToken.getUserId(), userToken.getToken());
    }

    @Override
    public UserToken findByToken(String token) {
        return TOKEN_STORE.get(token);
    }

    @Override
    public UserToken findValidByUserId(Long userId) {
        String token = USER_TOKEN_MAP.get(userId);
        if (token == null) {
            return null;
        }

        UserToken userToken = TOKEN_STORE.get(token);
        if (userToken == null) {
            return null;
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(userToken.getExpireTime())) {
            // 清理过期token
            TOKEN_STORE.remove(token);
            USER_TOKEN_MAP.remove(userId);
            return null;
        }

        return userToken;
    }

    @Override
    public void deleteToken(String token) {
        UserToken userToken = TOKEN_STORE.get(token);
        if (userToken == null) {
            return;
        }

        // 直接删除token
        TOKEN_STORE.remove(token);
        USER_TOKEN_MAP.remove(userToken.getUserId());
    }

    @Override
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // 遍历所有token，删除过期的
        TOKEN_STORE.entrySet().removeIf(entry -> {
            UserToken userToken = entry.getValue();
            if (now.isAfter(userToken.getExpireTime())) {
                USER_TOKEN_MAP.remove(userToken.getUserId());
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        String token = USER_TOKEN_MAP.get(userId);
        if (token == null) {
            return false;
        }

        TOKEN_STORE.remove(token);
        USER_TOKEN_MAP.remove(userId);
        return true;
    }
} 
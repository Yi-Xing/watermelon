package top.fblue.watermelon.auth.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.auth.jwt.JwtTokenService;
import top.fblue.auth.repository.TokenRevocationRepository;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.repository.AuthRepository;
import top.fblue.watermelon.auth.domain.user.repository.AuthRedisRepository;
import top.fblue.watermelon.auth.domain.user.repository.SsoUserQueryRepository;
import top.fblue.watermelon.auth.domain.user.service.AuthDomainService;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 基于 Redis 会话仓储和令牌撤销仓储的 SSO 认证领域服务实现。
 */
@Service
@RequiredArgsConstructor
public class AuthDomainServiceImpl implements AuthDomainService {

    /** SSO 会话、授权码及会话客户端关系仓储。 */
    private final AuthRedisRepository authRedisRepository;
    /** 全局会话及本地令牌撤销状态仓储。 */
    private final TokenRevocationRepository revocationRepository;
    /** SSO JWT 创建及校验服务。 */
    private final JwtTokenService jwtTokenService;
    /** SSO 用户查询仓储。 */
    private final SsoUserQueryRepository ssoUserQueryRepository;
    /** SSO 客户端会话撤销通知仓储。 */
    private final AuthRepository authRepository;
    /** SSO 会话和授权码有效期配置。 */
    private final AuthProperties authProperties;
    /** 一次性授权码使用的密码学安全随机数生成器。 */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo createSession(Long userId, String deviceCode) {
        if (userId == null) {
            throw badRequest("userId 不能为空");
        }
        if (authProperties.getSessionTtlSeconds() <= 0) {
            throw new IllegalStateException("auth.session-ttl-seconds 必须大于 0");
        }
        long now = Instant.now().getEpochSecond();
        long expireAt = now + authProperties.getSessionTtlSeconds();
        SsoSessionInfo session = SsoSessionInfo.builder()
                .sid(UUID.randomUUID().toString())
                .userId(userId)
                .deviceCode(deviceCode)
                .expireAtEpochSeconds(expireAt)
                .build();
        authRedisRepository.saveSession(session, authProperties.getSessionTtlSeconds());
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo requireActiveSession(String sid) {
        if (!StringUtils.hasText(sid) || revocationRepository.isSidRevoked(sid)) {
            throw unauthorized("全局登录会话无效或已退出");
        }
        SsoSessionInfo session = authRedisRepository.findSession(sid);
        if (session == null || session.getExpireAtEpochSeconds() <= Instant.now().getEpochSecond()) {
            throw unauthorized("全局登录会话不存在或已过期");
        }
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo getSession(String sid) {
        return StringUtils.hasText(sid) ? authRedisRepository.findSession(sid) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoPrincipal validateAccessToken(String token) {
        SsoPrincipal principal = jwtTokenService.parseAndValidate(token);
        if (revocationRepository.isSidRevoked(principal.getSid())
                || revocationRepository.isJtiRevoked(principal.getJti())) {
            throw unauthorized("登录会话已退出");
        }
        return principal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateCode(Long userId, String sid, String clientId,
                               String redirectUri, long sessionExpireAt) {
        SsoSessionInfo session = requireActiveSession(sid);
        if (!Objects.equals(session.getUserId(), userId)
                || session.getExpireAtEpochSeconds() != sessionExpireAt) {
            throw forbidden("登录会话与当前用户不匹配");
        }
        long ttl = Math.min(authProperties.getCodeTtlSeconds(),
                sessionExpireAt - Instant.now().getEpochSecond());
        if (ttl <= 0) {
            throw unauthorized("登录会话已过期");
        }
        AuthCodeInfo codeInfo = new AuthCodeInfo(userId, sid, clientId, redirectUri, sessionExpireAt);
        // 循环 3 次是为了处理授权码极小概率的碰撞
        for (int i = 0; i < 3; i++) {
            String code = randomToken();
            if (authRedisRepository.saveCode(code, codeInfo, ttl)) {
                return code;
            }
        }
        throw new IllegalStateException("生成唯一授权码失败");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthCodeInfo consumeCode(String code, String clientId, String redirectUri) {
        AuthCodeInfo codeInfo = authRedisRepository.getAndDeleteCode(code);
        if (codeInfo == null
                || !Objects.equals(codeInfo.getClientId(), clientId)
                || !Objects.equals(codeInfo.getRedirectUri(), redirectUri)) {
            throw badRequest("invalid_grant");
        }
        requireActiveSession(codeInfo.getSid());
        long ttl = codeInfo.getSessionExpireAt() - Instant.now().getEpochSecond();
        if (ttl <= 0) {
            throw badRequest("invalid_grant");
        }
        authRedisRepository.addSidClient(codeInfo.getSid(), clientId, ttl);
        return codeInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long userId) {
        return ssoUserQueryRepository.findById(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSessionClients(String sid) {
        if (!StringUtils.hasText(sid)) {
            return Collections.emptySet();
        }
        Set<String> clients = authRedisRepository.getSidClients(sid);
        return clients == null ? Collections.emptySet() : Set.copyOf(clients);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revokeSession(String sid, String eventId, String reason) {
        SsoSessionInfo session = authRedisRepository.findSession(sid);
        if (session == null) {
            return;
        }
        long ttl = session.getExpireAtEpochSeconds() - Instant.now().getEpochSecond();
        if (ttl > 0) {
            revocationRepository.revokeSid(sid, eventId == null ? reason : eventId, ttl);
        }
        TokenRevokeRequest revokeRequest = TokenRevokeRequest.builder()
                .eventId(eventId)
                .sid(sid)
                .sessionExpireAt(session.getExpireAtEpochSeconds())
                .reason(reason)
                .build();
        for (String clientId : getSessionClients(sid)) {
            authRepository.notifySystemRevokeSession(clientId, revokeRequest);
        }
    }

    /**
     * 生成 URL 安全的 256 位随机令牌。
     *
     * @return Base64 URL 编码的随机令牌
     */
    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 创建请求参数或授权码校验失败异常。
     *
     * @param message 错误信息
     * @return HTTP 400 对应的 SSO 异常
     */
    private SsoAuthException badRequest(String message) {
        return new SsoAuthException(ApiCodeEnum.BAD_REQUEST, message);
    }

    /**
     * 创建登录会话无效异常。
     *
     * @param message 错误信息
     * @return HTTP 401 对应的 SSO 异常
     */
    private SsoAuthException unauthorized(String message) {
        return new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, message);
    }

    /**
     * 创建当前身份无权执行操作的异常。
     *
     * @param message 错误信息
     * @return HTTP 403 对应的 SSO 异常
     */
    private SsoAuthException forbidden(String message) {
        return new SsoAuthException(ApiCodeEnum.FORBIDDEN, message);
    }
}

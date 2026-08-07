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
import top.fblue.watermelon.auth.domain.user.factory.AuthDomainFactory;
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

    /** SSO 认证领域对象工厂。 */
    private final AuthDomainFactory authDomainFactory;

    /** 一次性授权码使用的密码学安全随机数生成器。 */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo createSession(Long userId, String deviceCode) {
        // 1. 校验会话归属用户和会话有效期配置
        if (userId == null) {
            throw badRequest("userId 不能为空");
        }
        if (authProperties.getSessionTtlSeconds() <= 0) {
            throw new IllegalStateException("auth.session-ttl-seconds 必须大于 0");
        }

        // 2. 计算会话过期时间并创建领域对象
        long now = Instant.now().getEpochSecond();
        long expireAt = now + authProperties.getSessionTtlSeconds();
        SsoSessionInfo session = authDomainFactory.createSession(
                UUID.randomUUID().toString(), userId, deviceCode, expireAt);

        // 3. 持久化全局会话并返回
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
        // 1. 查询并校验授权码关联的全局会话
        SsoSessionInfo session = requireActiveSession(sid);
        if (!Objects.equals(session.getUserId(), userId)
                || session.getExpireAtEpochSeconds() != sessionExpireAt) {
            throw forbidden("登录会话与当前用户不匹配");
        }

        // 2. 计算不超过全局会话剩余时长的一次性授权码有效期
        long ttl = Math.min(authProperties.getCodeTtlSeconds(),
                sessionExpireAt - Instant.now().getEpochSecond());
        if (ttl <= 0) {
            throw unauthorized("登录会话已过期");
        }

        // 3. 创建授权码关联信息
        AuthCodeInfo codeInfo = new AuthCodeInfo(userId, sid, clientId, redirectUri, sessionExpireAt);

        // 4. 生成并原子保存唯一授权码，重试用于处理极小概率碰撞
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
        // 1. 原子读取并删除一次性授权码
        AuthCodeInfo codeInfo = authRedisRepository.getAndDeleteCode(code);

        // 2. 校验授权码绑定的客户端和回调地址
        if (codeInfo == null
                || !Objects.equals(codeInfo.getClientId(), clientId)
                || !Objects.equals(codeInfo.getRedirectUri(), redirectUri)) {
            throw badRequest("invalid_grant");
        }

        // 3. 校验授权码关联的全局会话及剩余有效期
        requireActiveSession(codeInfo.getSid());
        long ttl = codeInfo.getSessionExpireAt() - Instant.now().getEpochSecond();
        if (ttl <= 0) {
            throw badRequest("invalid_grant");
        }

        // 4. 记录客户端与全局会话的绑定关系，用于后续全局注销通知
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
        // 1. 查询全局会话；会话不存在时按幂等成功处理
        SsoSessionInfo session = authRedisRepository.findSession(sid);
        if (session == null) {
            return;
        }

        // 2. 在会话剩余有效期内写入用户中心撤销记录
        long ttl = session.getExpireAtEpochSeconds() - Instant.now().getEpochSecond();
        if (ttl > 0) {
            revocationRepository.revokeSid(sid, eventId == null ? reason : eventId, ttl);
        }

        // 3. 创建会话撤销通知
        TokenRevokeRequest revokeRequest = authDomainFactory.createTokenRevokeRequest(
                eventId, sid, session.getExpireAtEpochSeconds(), reason);

        // 4. 通知所有已绑定业务系统撤销本地会话
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

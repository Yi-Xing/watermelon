package top.fblue.watermelon.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.repository.AuthRepository;
import top.fblue.watermelon.auth.domain.user.service.AuthDomainService;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static top.fblue.auth.common.SsoConstants.USER_LOGOUT_REASON;

/**
 * 用户中心 SSO 授权应用服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsoAuthorizationApplicationServiceImpl implements SsoAuthorizationApplicationService {

    /** 认证领域服务，负责会话及一次性授权码的生命周期管理。 */
    private final AuthDomainService authDomainService;
    /** 认证基础设施仓储，负责向接入系统发送会话撤销通知。 */
    private final AuthRepository authRepository;
    /** 用户中心认证及 SSO 客户端配置。 */
    private final AuthProperties authProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo createSession(Long userId, String deviceCode) {
        if (userId == null) {
            throw badRequest("userId 不能为空");
        }
        return authDomainService.createSession(userId, deviceCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallbackResponse generateCallback(SsoPrincipal principal, CallbackRequest request) {
        if (principal == null || request == null || !StringUtils.hasText(request.getState())) {
            throw badRequest("授权请求不能为空");
        }
        String redirectUri = validateClientAndRedirect(request.getClientId(), request.getRedirectUri());
        SsoSessionInfo session = authDomainService.requireActiveSession(principal.getSid());
        if (!Objects.equals(session.getUserId(), principal.getUserId())) {
            throw forbidden("当前用户与全局会话不匹配");
        }
        String code = authDomainService.generateCode(principal.getUserId(), principal.getSid(),
                request.getClientId(), redirectUri, session.getExpireAtEpochSeconds());
        String callbackUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .queryParam("state", request.getState())
                .build()
                .encode()
                .toUriString();
        return CallbackResponse.builder().callbackUrl(callbackUrl).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeExchangeResponse exchangeCode(CodeExchangeRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw badRequest("invalid_grant");
        }
        String redirectUri = validateClientAndRedirect(request.getClientId(), request.getRedirectUri());
        AuthCodeInfo codeInfo = authDomainService.consumeCode(request.getCode(), request.getClientId(), redirectUri);
        User user = authDomainService.getUserById(codeInfo.getUserId());
        if (user == null) {
            throw badRequest("invalid_grant");
        }
        return CodeExchangeResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .sid(codeInfo.getSid())
                .sessionExpireAt(codeInfo.getSessionExpireAt())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revokeSession(LogoutRpcRequest request) {
        if (request == null || !StringUtils.hasText(request.getSid())
                || !StringUtils.hasText(request.getClientId())) {
            throw badRequest("sid 和 clientId 不能为空");
        }
        if (!authProperties.getClients().containsKey(request.getClientId())
                && !authProperties.getServerClientId().equals(request.getClientId())) {
            throw badRequest("未注册的 SSO Client");
        }
        SsoSessionInfo session = authDomainService.getSession(request.getSid());
        if (session == null) {
            return;
        }
        if (!authProperties.getServerClientId().equals(request.getClientId())
                && !authDomainService.getSessionClients(request.getSid()).contains(request.getClientId())) {
            throw forbidden("当前 Client 未绑定该全局会话");
        }
        String eventId = UUID.randomUUID().toString();
        String reason = defaultReason(request.getReason());
        Set<String> clients = authDomainService.revokeSession(request.getSid(), eventId, reason);
        TokenRevokeRequest revokeRequest = TokenRevokeRequest.builder()
                .eventId(eventId)
                .sid(request.getSid())
                .sessionExpireAt(session.getExpireAtEpochSeconds())
                .reason(reason)
                .build();
        for (String clientId : clients) {
            authRepository.notifySystemRevokeSession(clientId, revokeRequest);
        }
    }

    /**
     * 校验客户端是否可用，并确认回调地址在该客户端的白名单内。
     *
     * @param clientId SSO 客户端标识
     * @param redirectUri 待校验的回调地址
     * @return 规范化后的回调地址
     */
    private String validateClientAndRedirect(String clientId, String redirectUri) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(redirectUri)) {
            throw badRequest("clientId 和 redirectUri 不能为空");
        }
        AuthProperties.Client client = authProperties.getClients().get(clientId);
        if (client == null || !client.isEnabled()) {
            throw badRequest("未注册或已禁用的 SSO Client");
        }
        String canonicalRedirectUri = canonicalizeRedirectUri(redirectUri);
        boolean matched = client.getRedirectUris() != null && client.getRedirectUris().stream()
                .map(this::canonicalizeRedirectUri)
                .anyMatch(canonicalRedirectUri::equals);
        if (!matched) {
            throw badRequest("redirectUri 未注册");
        }
        return canonicalRedirectUri;
    }

    /**
     * 校验回调地址的结构和协议，并将其转换为 ASCII URI 字符串。
     *
     * @param redirectUri 原始回调地址
     * @return 规范化后的回调地址
     */
    private String canonicalizeRedirectUri(String redirectUri) {
        try {
            URI uri = URI.create(redirectUri);
            if (!uri.isAbsolute() || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw badRequest("redirectUri 格式不合法");
            }
            boolean https = "https".equalsIgnoreCase(uri.getScheme());
            boolean allowedHttp = "http".equalsIgnoreCase(uri.getScheme())
                    && authProperties.isAllowInsecureRedirects();
            if (!https && !allowedHttp) {
                throw badRequest("redirectUri 必须使用 HTTPS");
            }
            return uri.toASCIIString();
        } catch (IllegalArgumentException e) {
            throw badRequest("redirectUri 格式不合法");
        }
    }

    /**
     * 获取会话注销原因；请求未提供时使用默认用户注销原因。
     *
     * @param reason 请求中的注销原因
     * @return 最终使用的注销原因
     */
    private String defaultReason(String reason) {
        return StringUtils.hasText(reason) ? reason : USER_LOGOUT_REASON;
    }

    /**
     * 创建参数或业务校验失败异常。
     *
     * @param message 异常提示信息
     * @return HTTP 400 SSO 异常
     */
    private SsoAuthException badRequest(String message) {
        return new SsoAuthException(ApiCodeEnum.BAD_REQUEST, message);
    }

    /**
     * 创建无权操作当前资源的异常。
     *
     * @param message 异常提示信息
     * @return HTTP 403 SSO 异常
     */
    private SsoAuthException forbidden(String message) {
        return new SsoAuthException(ApiCodeEnum.FORBIDDEN, message);
    }
}

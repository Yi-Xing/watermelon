package top.fblue.watermelon.auth.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

/**
 * 用户中心 SSO 授权请求和会话关系校验器。
 */
@Component
@RequiredArgsConstructor
public class SsoAuthorizationValidator {

    /** 用户中心认证及 SSO 客户端配置。 */
    private final AuthProperties authProperties;

    /**
     * 校验创建会话所需的用户 ID。
     *
     * @param userId 用户 ID
     */
    public void validateCreateSession(Long userId) {
        if (userId == null || userId <= 0) {
            throw badRequest("userId 不能为空");
        }
    }

    /**
     * 校验授权请求，并返回规范化后的回调地址。
     *
     * @param principal 当前登录主体
     * @param request 授权请求
     * @return 规范化后的回调地址
     */
    public String validateAuthorizationRequest(SsoPrincipal principal, CallbackRequest request) {
        if (principal == null || request == null || !StringUtils.hasText(request.getState())) {
            throw badRequest("授权请求不能为空");
        }
        return validateClientAndRedirect(request.getClientId(), request.getRedirectUri());
    }

    /**
     * 校验当前登录主体是否属于目标全局会话。
     *
     * @param principal 当前登录主体
     * @param session 全局会话
     */
    public void validateSessionOwner(SsoPrincipal principal, SsoSessionInfo session) {
        if (!Objects.equals(session.getUserId(), principal.getUserId())) {
            throw forbidden("当前用户与全局会话不匹配");
        }
    }

    /**
     * 校验授权码兑换请求，并返回规范化后的回调地址。
     *
     * @param request 授权码兑换请求
     * @return 规范化后的回调地址
     */
    public String validateCodeExchangeRequest(CodeExchangeRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw badRequest("invalid_grant");
        }
        return validateClientAndRedirect(request.getClientId(), request.getRedirectUri());
    }

    /**
     * 校验全局会话注销请求。
     *
     * @param request 注销请求
     */
    public void validateLogoutRequest(LogoutRpcRequest request) {
        if (request == null || !StringUtils.hasText(request.getSid())
                || !StringUtils.hasText(request.getClientId())) {
            throw badRequest("sid 和 clientId 不能为空");
        }
        if (!authProperties.getClients().containsKey(request.getClientId())
                && !authProperties.getServerClientId().equals(request.getClientId())) {
            throw badRequest("未注册的 SSO Client");
        }
    }

    /**
     * 校验发起注销的客户端是否已绑定目标全局会话。
     *
     * @param request 注销请求
     * @param sessionClients 会话已绑定的客户端集合
     */
    public void validateLogoutClientBinding(LogoutRpcRequest request, Set<String> sessionClients) {
        if (!authProperties.getServerClientId().equals(request.getClientId())
                && !sessionClients.contains(request.getClientId())) {
            throw forbidden("当前 Client 未绑定该全局会话");
        }
    }

    /**
     * 校验客户端是否可用，并确认回调地址在白名单内。
     *
     * @param clientId SSO 客户端标识
     * @param redirectUri 待校验回调地址
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
     * 校验回调地址结构和协议，并转换为 ASCII URI 字符串。
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
        } catch (IllegalArgumentException exception) {
            throw badRequest("redirectUri 格式不合法");
        }
    }

    /**
     * 创建参数校验失败异常。
     *
     * @param message 异常提示
     * @return HTTP 400 SSO 异常
     */
    private SsoAuthException badRequest(String message) {
        return new SsoAuthException(ApiCodeEnum.BAD_REQUEST, message);
    }

    /**
     * 创建无权操作当前会话的异常。
     *
     * @param message 异常提示
     * @return HTTP 403 SSO 异常
     */
    private SsoAuthException forbidden(String message) {
        return new SsoAuthException(ApiCodeEnum.FORBIDDEN, message);
    }
}

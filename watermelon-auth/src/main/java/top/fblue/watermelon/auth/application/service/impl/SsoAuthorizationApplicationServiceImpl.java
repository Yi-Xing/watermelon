package top.fblue.watermelon.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.converter.SsoAuthorizationConverter;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;
import top.fblue.watermelon.auth.application.validator.SsoAuthorizationValidator;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.service.AuthDomainService;

import java.util.UUID;

import static top.fblue.auth.common.SsoConstants.USER_LOGOUT_REASON;

/**
 * 用户中心 SSO 授权应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class SsoAuthorizationApplicationServiceImpl implements SsoAuthorizationApplicationService {

    /** 认证领域服务，负责会话及一次性授权码的生命周期管理。 */
    private final AuthDomainService authDomainService;

    /** SSO 授权请求和会话关系校验器。 */
    private final SsoAuthorizationValidator authorizationValidator;

    /** SSO 授权结果转换器。 */
    private final SsoAuthorizationConverter authorizationConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public SsoSessionInfo createSession(Long userId, String deviceCode) {
        // 1. 校验创建会话所需参数
        authorizationValidator.validateCreateSession(userId);

        // 2. 通过领域服务创建全局会话
        return authDomainService.createSession(userId, deviceCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallbackResponse generateCallback(SsoPrincipal principal, CallbackRequest request) {
        // 1. 校验授权请求并规范化客户端回调地址
        String redirectUri = authorizationValidator.validateAuthorizationRequest(principal, request);

        // 2. 查询并校验当前全局会话
        SsoSessionInfo session = authDomainService.requireActiveSession(principal.getSid());
        authorizationValidator.validateSessionOwner(principal, session);

        // 3. 为当前客户端签发一次性授权码
        String code = authDomainService.generateCode(principal.getUserId(), principal.getSid(),
                request.getClientId(), redirectUri, session.getExpireAtEpochSeconds());

        // 4. 转换为客户端回调响应
        return authorizationConverter.toCallbackResponse(redirectUri, code, request.getState());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeExchangeResponse exchangeCode(CodeExchangeRequest request) {
        // 1. 校验兑换请求并规范化客户端回调地址
        String redirectUri = authorizationValidator.validateCodeExchangeRequest(request);

        // 2. 原子消费一次性授权码
        AuthCodeInfo codeInfo = authDomainService.consumeCode(request.getCode(), request.getClientId(), redirectUri);

        // 3. 查询授权码对应用户
        User user = authDomainService.getUserById(codeInfo.getUserId());
        if (user == null) {
            throw new SsoAuthException(ApiCodeEnum.BAD_REQUEST, "invalid_grant");
        }

        // 4. 转换为授权码兑换响应
        return authorizationConverter.toCodeExchangeResponse(user, codeInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revokeSession(LogoutRpcRequest request) {
        // 1. 校验注销请求和客户端注册状态
        authorizationValidator.validateLogoutRequest(request);

        // 2. 查询全局会话；会话已不存在时按幂等成功处理
        SsoSessionInfo session = authDomainService.getSession(request.getSid());
        if (session == null) {
            return;
        }

        // 3. 校验客户端是否有权注销该会话
        authorizationValidator.validateLogoutClientBinding(
                request, authDomainService.getSessionClients(request.getSid()));

        // 4. 生成通知事件并通过领域服务注销会话
        String eventId = UUID.randomUUID().toString();
        String reason = defaultReason(request.getReason());
        authDomainService.revokeSession(request.getSid(), eventId, reason);
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

}

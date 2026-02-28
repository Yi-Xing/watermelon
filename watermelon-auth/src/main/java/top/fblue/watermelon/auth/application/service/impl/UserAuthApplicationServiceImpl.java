package top.fblue.watermelon.auth.application.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.fblue.common.exception.BusinessException;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.service.UserAuthApplicationService;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.common.utils.TokenUtil;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.service.AuthDomainService;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;
import top.fblue.watermelon.auth.infrastructure.config.JwtAuthConfig;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;

import java.net.URI;

/**
 * 鉴权应用服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthApplicationServiceImpl implements UserAuthApplicationService {

    private final AuthDomainService authDomainService;
    private final AuthProperties authProperties;
    private final JwtAuthConfig jwtAuthConfig;

    @Override
    public UserDTO validateToken(String token) {
        // 1. 本地解析 JWT（签名校验 + 过期校验），失败则抛出异常
        Claims claims = TokenUtil.parseToken(token, jwtAuthConfig.getSecret());

        // 2. 检查 jti 是否在黑名单（经由领域服务 → 仓储）
        String jti = TokenUtil.getJti(claims);
        if (StringUtils.hasText(jti) && authDomainService.isJtiRevoked(jti)) {
            throw new BusinessException("token 已被吊销");
        }

        Long userId = TokenUtil.getUserId(claims);
        long expiresIn = TokenUtil.getRemainingSeconds(claims);
        return UserDTO.builder()
                .userId(userId)
                .jti(jti)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public CallbackResponse generateCallback(Long userId, String jti, long expiresIn,
                                             String returnUrl, String state, String cookieState) {
        // 1. 校验 return_url 域名白名单
        validateReturnUrl(returnUrl);

        // 2. 校验 state 与 cookie 一致
        if (!StringUtils.hasText(cookieState) || !cookieState.equals(state)) {
            log.warn("state 校验失败，请求 state: {}, cookie state: {}", state, cookieState);
            throw new BusinessException("state 校验失败，请重新登录");
        }

        // 3. 生成授权码（领域逻辑）
        String code = authDomainService.generateCode(userId, jti, expiresIn);

        // 4. 拼接回调地址
        String separator = returnUrl.contains("?") ? "&" : "?";
        String callbackUrl = returnUrl + separator + "code=" + code + "&state=" + state;

        log.info("生成回调地址成功，userId: {}, callbackUrl: {}", userId, callbackUrl);
        return CallbackResponse.builder().callbackUrl(callbackUrl).build();
    }

    @Override
    public CodeExchangeResponse exchangeCode(CodeExchangeRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException("code 不能为空");
        }

        // 1. 消费授权码（领域逻辑：取出 + 删除 + 记录 jti-system）
        AuthCodeInfo codeInfo = authDomainService.consumeCode(request.getCode(), request.getCallerSystem());
        if (codeInfo == null) {
            throw new BusinessException("code 无效或已过期");
        }

        // 2. 查询用户信息（领域逻辑）
        User user = authDomainService.getUserById(codeInfo.getUserId());

        log.info("code 换用户信息成功，userId: {}, callerSystem: {}", codeInfo.getUserId(), request.getCallerSystem());
        return CodeExchangeResponse.builder()
                .userId(codeInfo.getUserId())
                .username(user != null ? user.getUsername() : null)
                .jti(codeInfo.getJti())
                .expiresIn(codeInfo.getExpiresIn())
                .build();
    }

    @Override
    public void revokeToken(LogoutRpcRequest request) {
        if (!StringUtils.hasText(request.getJti())) {
            throw new BusinessException("jti 不能为空");
        }
        // 领域逻辑：黑名单 + 通知各系统
        authDomainService.revokeToken(request.getJti(), request.getDeviceCode(), request.getExpiresIn());
    }

    /**
     * 校验 return_url 域名是否在白名单中
     */
    private void validateReturnUrl(String returnUrl) {
        if (authProperties.getCallbackDomainWhitelist().isEmpty()) {
            throw new BusinessException("回调地址白名单未配置");
        }
        try {
            URI uri = URI.create(returnUrl);
            String host = uri.getHost();
            boolean allowed = authProperties.getCallbackDomainWhitelist().stream()
                    .anyMatch(domain -> host != null && (host.equals(domain) || host.endsWith("." + domain)));
            if (!allowed) {
                log.warn("return_url 域名不在白名单: {}", returnUrl);
                throw new BusinessException("return_url 域名不合法");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("return_url 格式不合法");
        }
    }
}

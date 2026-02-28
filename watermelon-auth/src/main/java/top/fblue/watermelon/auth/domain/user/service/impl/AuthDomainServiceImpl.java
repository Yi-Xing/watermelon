package top.fblue.watermelon.auth.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.api.request.TokenRevokeRequest;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.repository.AuthRedisRepository;
import top.fblue.watermelon.auth.domain.user.repository.AuthRepository;
import top.fblue.watermelon.auth.domain.user.service.AuthDomainService;

import java.util.Set;
import java.util.UUID;

/**
 * Auth 领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthDomainServiceImpl implements AuthDomainService {

    private final AuthRedisRepository authRedisRepository;
    private final AuthRepository authRepository;

    @Override
    public String generateCode(Long userId, String jti, long expiresIn) {
        String code = UUID.randomUUID().toString().replace("-", "");
        // code 本身的 TTL 使用 expiresIn（与 token 剩余时间一致，取两者较小值由上层保证）
        authRedisRepository.saveCode(code, userId, jti, expiresIn, expiresIn);
        log.debug("生成授权码，userId: {}, jti: {}, code: {}", userId, jti, code);
        return code;
    }

    @Override
    public AuthCodeInfo consumeCode(String code, String callerSystem) {
        AuthCodeInfo codeInfo = authRedisRepository.getAndDeleteCode(code);
        if (codeInfo == null) {
            log.warn("授权码不存在或已过期: {}", code);
            return null;
        }
        // 记录 jti -> callerSystem（用于退出登录时通知）
        String jti = codeInfo.getJti();
        if (StringUtils.hasText(jti) && StringUtils.hasText(callerSystem)) {
            authRedisRepository.addJtiSystem(jti, callerSystem, codeInfo.getExpiresIn());
            log.debug("记录 jti-system 映射，jti: {}, system: {}", jti, callerSystem);
        }
        return codeInfo;
    }

    @Override
    public User getUserById(Long userId) {
        return authRepository.findUserById(userId);
    }

    @Override
    public boolean isJtiRevoked(String jti) {
        return authRedisRepository.isJtiRevoked(jti);
    }

    @Override
    public void revokeToken(String jti, String deviceCode, long expiresIn) {
        // 1. 将 jti 加入黑名单
        authRedisRepository.revokeJti(jti, deviceCode, expiresIn);
        log.info("jti 已加入黑名单，jti: {}, deviceCode: {}", jti, deviceCode);

        // 2. 查询 jti 关联的所有系统，逐一通知
        //    TODO: 后续可优化为 MQ 异步通知
        Set<String> systems = authRedisRepository.getJtiSystems(jti);
        if (systems == null || systems.isEmpty()) {
            log.debug("jti 无关联系统，跳过通知，jti: {}", jti);
            return;
        }

        TokenRevokeRequest revokeRequest = TokenRevokeRequest.builder()
                .jti(jti)
                .expiresIn(expiresIn)
                .deviceCode(deviceCode)
                .build();

        for (String systemCode : systems) {
            authRepository.notifySystemRevokeToken(systemCode, revokeRequest);
        }
    }
}

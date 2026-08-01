package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.auth.jwt.JwtTokenService;
import top.fblue.auth.repository.TokenRevocationRepository;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.UserAuthApplicationService;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.auth.context.SsoHttpContext;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.utils.TokenUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import lombok.extern.slf4j.Slf4j;
import top.fblue.watermelon.infrastructure.config.SystemConfig;
import top.fblue.watermelon.common.enums.StateEnum;

import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static top.fblue.auth.common.SsoConstants.USER_LOGOUT_REASON;

/**
 * 登录应用服务实现类
 */
@Service
@Slf4j
public class UserAuthApplicationServiceImpl implements UserAuthApplicationService {

    /** 用户领域服务。 */
    @Resource
    private UserDomainService userDomainService;

    /** 用户领域对象转换器。 */
    @Resource
    private UserConverter userConverter;

    /** SSO JWT 创建及校验服务。 */
    @Resource
    private JwtTokenService jwtTokenService;

    /** 会话和令牌撤销状态仓储。 */
    @Resource
    private TokenRevocationRepository tokenRevocationRepository;

    /** 用户中心 SSO 会话及授权应用服务。 */
    @Resource
    private SsoAuthorizationApplicationService ssoAuthorizationApplicationService;

    /** 角色领域服务。 */
    @Resource
    private RoleDomainService roleDomainService;

    /** 资源权限领域服务。 */
    @Resource
    private ResourceDomainService resourceDomainService;

    /** 当前系统编码等基础配置。 */
    @Resource
    private SystemConfig systemConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 调用领域服务进行登录验证
        User user = userDomainService.login(loginDTO.getAccount(), loginDTO.getPassword());

        SsoSessionInfo session = ssoAuthorizationApplicationService.createSession(user.getId(), null);
        String token = jwtTokenService.createToken(user.getId(), user.getUsername(),
                session.getSid(), session.getExpireAtEpochSeconds());

        // 使用转换器构建用户信息
        UserVO userInfo = userConverter.toVO(user);

        return LoginVO.builder()
                .userInfo(userInfo)
                .token(token)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(String authHeader) {
        // 提取token
        String token = TokenUtil.extractTokenFromHeader(authHeader);

        SsoPrincipal principal = validatePrincipal(token);
        ssoAuthorizationApplicationService.revokeSession(LogoutRpcRequest.builder()
                .sid(principal.getSid())
                .clientId("watermelon")
                .jti(principal.getJti())
                .reason(USER_LOGOUT_REASON)
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String refreshToken(String authHeader) {
        throw new SsoAuthException(ApiCodeEnum.BAD_REQUEST,
                "SSO V1 不支持刷新 token，请重新登录");
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public CurrentUserVO getCurrentUser() {
        // 1. 通过UserContext获取当前登录用户
        SsoPrincipal principal = SsoHttpContext.getCurrentUserInfo();
        UserTokenDTO userToken = toUserTokenDTO(principal, null);

        // 2. 查询用户信息
        User user = userDomainService.getUserById(userToken.getUserId());

        // 3. 获取用户关联的角色ID列表
        List<Long> roleIds = userDomainService.getUserRoles(user.getId());

        // 4. 获取所有角色的资源权限
        List<Long> resourceIds = roleDomainService.getRoleResourceIdsByRoleIds(roleIds);


        // 5. 获取指定code前缀的页面和按钮资源
        List<ResourceNode> resourcesList = resourceDomainService.getResourcesByCodePrefixAndTypesAndIds(
                systemConfig.getCode(),
                List.of(ResourceTypeEnum.PAGE.getCode(), ResourceTypeEnum.BUTTON.getCode()),
                resourceIds,
                StateEnum.ENABLE.getCode()
        );

        // 6. 数据组装
        return userConverter.toVO(user, userToken, resourcesList, systemConfig.getCode());
    }

    /**
     * 验证token有效性并获取 UserToken
     */
    @Override
    public UserTokenDTO validateToken(String token) {
        return toUserTokenDTO(validatePrincipal(token), token);
    }

    /**
     * 判断当前登录用户是否有指定接口资源的code权限
     */
    @Override
    public boolean hasPermission(String resourceCode) {
        // 1. 获取当前登录用户ID
        Long currentUserId = SsoHttpContext.getCurrentUserId();

        // 2. 获取用户关联的角色ID列表
        List<Long> roleIds = userDomainService.getUserRoles(currentUserId);
        if (roleIds == null || roleIds.isEmpty()) {
            // 用户没有角色，无权限
            return false;
        }

        // 3. 获取所有角色的资源权限
        List<Long> resourceIds = roleDomainService.getRoleResourceIdsByRoleIds(roleIds);

        if (resourceIds == null || resourceIds.isEmpty()) {
            // 角色没有资源权限
            return false;
        }

        // 4. 直接查询数据库中是否存在匹配的资源权限
        return resourceDomainService.existsAPIResourceByCodeAndIds(resourceCode, resourceIds);
    }

    /**
     * 解析并校验访问令牌，同时检查 SID 和 JTI 是否已被撤销。
     *
     * @param token 待校验的 JWT 访问令牌
     * @return 校验通过的 SSO 用户身份
     */
    private SsoPrincipal validatePrincipal(String token) {
        SsoPrincipal principal = jwtTokenService.parseAndValidate(token);
        if (tokenRevocationRepository.isSidRevoked(principal.getSid())
                || tokenRevocationRepository.isJtiRevoked(principal.getJti())) {
            throw new SsoAuthException(ApiCodeEnum.UNAUTHORIZED,
                    "登录会话已退出");
        }
        return principal;
    }

    /**
     * 将 SSO 用户身份转换为兼容现有业务接口的用户令牌 DTO。
     *
     * @param principal SSO 用户身份
     * @param token 原始访问令牌；无需回传时可为 {@code null}
     * @return 用户令牌 DTO
     */
    private UserTokenDTO toUserTokenDTO(SsoPrincipal principal, String token) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime createdTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(principal.getIssuedAtEpochSeconds()), zoneId);
        LocalDateTime expireTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(principal.getExpiresAtEpochSeconds()), zoneId);
        return UserTokenDTO.builder()
                .userId(principal.getUserId())
                .sid(principal.getSid())
                .jti(principal.getJti())
                .token(token)
                .createdTime(createdTime)
                .expireTime(expireTime)
                .build();
    }
}

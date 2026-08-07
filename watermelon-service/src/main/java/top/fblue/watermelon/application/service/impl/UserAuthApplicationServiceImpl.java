package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.auth.context.SsoHttpContext;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.auth.jwt.JwtTokenService;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.auth.domain.permission.service.PermissionQueryDomainService;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.domain.user.service.AuthDomainService;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;
import top.fblue.watermelon.application.converter.UserAuthConverter;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.UserAuthApplicationService;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.utils.TokenUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;

import static top.fblue.auth.common.SsoConstants.USER_LOGOUT_REASON;

/**
 * 用户中心登录、退出及当前用户权限应用服务实现。
 */
@Service
public class UserAuthApplicationServiceImpl implements UserAuthApplicationService {

    /** 用户领域服务。 */
    @Resource
    private UserDomainService userDomainService;

    /** 用户领域对象转换器。 */
    @Resource
    private UserConverter userConverter;

    /** 用户登录及 SSO 会话对象转换器。 */
    @Resource
    private UserAuthConverter userAuthConverter;

    /** SSO JWT 创建服务。 */
    @Resource
    private JwtTokenService jwtTokenService;

    /** SSO 会话及访问令牌领域服务。 */
    @Resource
    private AuthDomainService authDomainService;

    /** 用户中心 SSO 会话及授权应用服务。 */
    @Resource
    private SsoAuthorizationApplicationService ssoAuthorizationApplicationService;

    /** 用户中心自身的 SSO Client 配置。 */
    @Resource
    private AuthProperties authProperties;

    /** 权限查询领域服务。 */
    @Resource
    private PermissionQueryDomainService permissionQueryDomainService;

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 调用领域服务验证用户账号和密码
        User user = userDomainService.login(loginDTO.getAccount(), loginDTO.getPassword());

        // 2. 创建全局 SSO 会话
        SsoSessionInfo session = ssoAuthorizationApplicationService.createSession(user.getId(), null);

        // 3. 为当前全局会话签发用户中心访问令牌
        String token = jwtTokenService.createToken(user.getId(), user.getUsername(),
                session.getSid(), session.getExpireAtEpochSeconds());

        // 4. 转换用户信息和登录响应
        UserVO userInfo = userConverter.toVO(user);
        return userAuthConverter.toLoginVO(userInfo, token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(String authHeader) {
        // 1. 从请求头提取并校验访问令牌
        String token = TokenUtil.extractTokenFromHeader(authHeader);
        SsoPrincipal principal = validatePrincipal(token);

        // 2. 转换并提交全局会话注销请求
        ssoAuthorizationApplicationService.revokeSession(userAuthConverter.toLogoutRequest(
                principal, authProperties.getServerClientId(), USER_LOGOUT_REASON));
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
     * 获取当前登录用户信息及其可见页面、按钮资源。
     *
     * @return 当前登录用户信息和前端资源权限
     */
    @Override
    public CurrentUserVO getCurrentUser() {
        // 1. 从 SSO 上下文获取当前登录用户
        SsoPrincipal principal = SsoHttpContext.getCurrentUserInfo();
        UserTokenDTO userToken = userAuthConverter.toUserTokenDTO(principal, null);

        // 2. 查询用户信息
        User user = userDomainService.getUserById(userToken.getUserId());

        // 3. 通过 auth 权限领域服务查询当前系统的有效权限快照
        PermissionSnapshot permissionSnapshot = permissionQueryDomainService.getPermissionSnapshot(
                user.getId(), authProperties.getServerClientId());

        // 4. 转换并返回当前用户信息；接口权限不会写入前端 VO
        return userConverter.toVO(user, userToken, permissionSnapshot);
    }

    /**
     * 校验访问令牌并转换为现有业务接口使用的令牌 DTO。
     *
     * @param token JWT 访问令牌
     * @return 校验通过的用户令牌信息
     */
    @Override
    public UserTokenDTO validateToken(String token) {
        // 1. 解析并校验访问令牌
        SsoPrincipal principal = validatePrincipal(token);

        // 2. 转换为兼容现有业务接口的令牌 DTO
        return userAuthConverter.toUserTokenDTO(principal, token);
    }

    /**
     * 解析并校验访问令牌，同时检查 SID 和 JTI 是否已被撤销。
     *
     * @param token 待校验的 JWT 访问令牌
     * @return 校验通过的 SSO 用户身份
     */
    private SsoPrincipal validatePrincipal(String token) {
        return authDomainService.validateAccessToken(token);
    }

}

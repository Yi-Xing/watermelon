package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.AuthApplicationService;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.context.UserContext;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.utils.TokenUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserToken;
import top.fblue.watermelon.domain.user.service.TokenDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 登录应用服务实现类
 */
@Service
@Slf4j
public class AuthApplicationServiceImpl implements AuthApplicationService {

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private UserConverter userConverter;

    @Resource
    private TokenDomainService tokenDomainService;

    @Resource
    private RoleDomainService roleDomainService;

    @Resource
    private ResourceDomainService resourceDomainService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 调用领域服务进行登录验证
        User user = userDomainService.login(loginDTO.getAccount(), loginDTO.getPassword());

        // 生成并存储token
        String token = tokenDomainService.generateToken(user);

        // 使用转换器构建用户信息
        UserVO userInfo = userConverter.toVO(user);

        return LoginVO.builder()
                .userInfo(userInfo)
                .token(token)
                .build();
    }

    @Override
    public void logout(String authHeader) {
        // 提取token
        String token = TokenUtil.extractTokenFromHeader(authHeader);

        // 先验证token是否存在
        tokenDomainService.validateToken(token);

        // 使token失效
        tokenDomainService.invalidateToken(token);
    }

    @Override
    public String refreshToken(String authHeader) {
        // 提取token
        String token = TokenUtil.extractTokenFromHeader(authHeader);

        // 验证原token是否有效
        tokenDomainService.validateToken(token);

        // 刷新token
        return tokenDomainService.refreshToken(token);
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public CurrentUserVO getCurrentUser() {
        // 通过UserContext获取当前登录用户
        UserTokenDTO userToken = UserContext.getCurrentUserInfo();

        User user = userDomainService.getUserById(userToken.getUserId());
        // 之后需要返回用户可使用的资源
        return userConverter.toVO(user, userToken);
    }

    /**
     * 验证token有效性并获取 UserToken
     */
    @Override
    public UserToken validateToken(String token) {
        return tokenDomainService.validateToken(token);
    }

    /**
     * 判断当前登录用户是否有指定接口资源的code权限
     */
    @Override
    public boolean hasPermission(String resourceCode) {
        // 1. 获取当前登录用户ID
        Long currentUserId = UserContext.getCurrentUserId();

        // 2. 获取用户关联的角色ID列表
        List<Long> roleIds = userDomainService.getUserRoles(currentUserId);
        if (roleIds == null || roleIds.isEmpty()) {
            // 用户没有角色，无权限
            return false;
        }

        // 3. 获取所有角色的资源权限
        List<Long> resourceIds = roleDomainService.getRoleResourceIdsByRoleIds(roleIds);

        if (resourceIds == null ||resourceIds.isEmpty()) {
            // 角色没有资源权限
            return false;
        }

        // 4. 直接查询数据库中是否存在匹配的资源权限
        return resourceDomainService.existsAPIResourceByCodeAndIds(resourceCode, resourceIds);
    }
}
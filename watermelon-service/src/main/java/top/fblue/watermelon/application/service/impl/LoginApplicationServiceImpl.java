package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.LoginApplicationService;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.context.UserContext;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.utils.TokenUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.TokenDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;

/**
 * 登录应用服务实现类
 */
@Service
public class LoginApplicationServiceImpl implements LoginApplicationService {

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private UserConverter userConverter;

    @Resource
    private TokenDomainService tokenDomainService;

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
    public CurrentUserVO getCurrentUser(){
        // 通过UserContext获取当前登录用户
        UserTokenDTO userToken = UserContext.getCurrentUserInfo();

        User user = userDomainService.getUserById(userToken.getUserId());
        // 之后需要返回用户可使用的资源
        return userConverter.toVO(user,userToken);
    }
}
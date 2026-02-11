package top.fblue.watermelon.auth.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.auth.application.service.AuthCommonApplicationService;
import top.fblue.watermelon.auth.common.dto.UserDTO;

/**
 * 登录应用服务实现类
 */
@Service
@Slf4j
public class AuthCommonApplicationServiceImpl implements AuthCommonApplicationService {

    @Override
    public void logout(String authHeader) {
        return;
    }

    @Override
    public String refreshToken(String authHeader) {
        return "";
    }

    /**
     * 验证token有效性并获取 UserToken
     */
    @Override
    public UserDTO validateToken(String token) {
        return null;
    }
}
package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.LoginApplicationService;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 登录应用服务实现类
 */
@Service
public class LoginApplicationServiceImpl implements LoginApplicationService {

    @Resource
    private  UserDomainService userDomainService;
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        
        // 根据账号查找用户（支持手机号、邮箱）
        User user = userDomainService.findByAccount(account);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 验证密码
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        // 检查用户状态
        if (!StateEnum.ENABLE.getCode().equals(user.getState())) {
            throw new IllegalArgumentException("用户已被禁用");
        }
        
        // 生成token（这里使用简单的UUID，实际项目中应该使用JWT）
        String token = UUID.randomUUID().toString();
        
        // 构建用户信息
        UserVO userInfo = UserVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .state(user.getState())
                .stateDesc(StateEnum.getDescByCode(user.getState()))
                .remark(user.getRemark())
                .createdTime(user.getCreatedTime() != null ? user.getCreatedTime().toString() : null)
                .updatedTime(user.getUpdatedTime() != null ? user.getUpdatedTime().toString() : null)
                .build();
        
        return LoginVO.builder()
                .userInfo(userInfo)
                .token(token)
                .build();
    }
    
    @Override
    public boolean logout(String token) {
        // TODO: 实现token失效逻辑
        // 实际项目中应该将token加入黑名单或从Redis中删除
        return true;
    }
}
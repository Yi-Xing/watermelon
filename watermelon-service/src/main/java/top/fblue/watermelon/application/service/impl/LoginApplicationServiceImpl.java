package top.fblue.watermelon.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.LoginApplicationService;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 登录应用服务实现类
 */
@Service
@RequiredArgsConstructor
public class LoginApplicationServiceImpl implements LoginApplicationService {
    
    private final UserDomainService userDomainService;
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        
        // 验证账号格式（手机号或邮箱）
        if (!isValidPhone(account) && !isValidEmail(account)) {
            throw new BusinessException("请输入正确的手机号或邮箱格式");
        }
        
        // 根据账号查找用户（支持手机号、邮箱）
        User user = userDomainService.findByAccount(account);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证密码
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        // 检查用户状态
        if (!StateEnum.ENABLE.getCode().equals(user.getState())) {
            throw new BusinessException("用户已被禁用");
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
        
        // 获取用户权限（这里简化处理，实际应该从角色权限中获取）
        List<String> permissions = new ArrayList<>();
        // TODO: 从用户角色中获取权限列表
        
        return LoginVO.builder()
                .userInfo(userInfo)
                .token(token)
                .permissions(permissions)
                .build();
    }
    
    @Override
    public boolean logout(String token) {
        // TODO: 实现token失效逻辑
        // 实际项目中应该将token加入黑名单或从Redis中删除
        return true;
    }
    
    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
} 
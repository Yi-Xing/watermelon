package top.fblue.watermelon.application.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.UserApplicationService;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.converter.UserConverterInterface;
import top.fblue.watermelon.domain.user.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户应用服务实现
 */
@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private  UserDomainService userDomainService; // 只依赖领域服务
    @Resource
    private  UserConverterInterface userConverter;
    
    @Override
    @Transactional
    public UserVO createUser(CreateUserDTO createUserDTO) {
        // 设置默认值，避免数据库存储null
        String phone = StringUtil.getNonEmptyString(createUserDTO.getPhone());
        String email = StringUtil.getNonEmptyString(createUserDTO.getEmail());
        String password = StringUtil.getNonEmptyString(createUserDTO.getPassword());
        String remark = StringUtil.getNonEmptyString(createUserDTO.getRemark());
        
        // 调用领域服务创建用户
        User user = userDomainService.createUser(
                createUserDTO.getName(),
                email,
                phone,
                password,
                createUserDTO.getState(),
                remark
        );
        
        // 返回VO
        return userConverter.toVO(user);
    }
    
    @Override
    public UserVO getUserById(Long id) {
        User user = userDomainService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return userConverter.toVO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserVO> getAllUsers() {
        return userDomainService.getAllUsers().stream()
                .map(userConverter::toVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserVO updateUserEmail(Long userId, String newEmail) {
        // 调用领域服务更新邮箱
        User user = userDomainService.updateUserEmail(userId, newEmail);
        return userConverter.toVO(user);
    }
    
    @Override
    public UserVO activateUser(Long userId) {
        // 调用领域服务激活用户
        User user = userDomainService.activateUser(userId);
        return userConverter.toVO(user);
    }
    
    @Override
    public UserVO deactivateUser(Long userId) {
        // 调用领域服务停用用户
        User user = userDomainService.deactivateUser(userId);
        return userConverter.toVO(user);
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        // 调用领域服务删除用户
        return userDomainService.deleteUser(userId);
    }
}

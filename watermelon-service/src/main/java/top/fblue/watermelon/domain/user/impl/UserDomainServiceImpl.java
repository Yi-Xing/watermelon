package top.fblue.watermelon.domain.user.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.user.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * 用户领域服务实现
 */
@Service
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private  UserRepository userRepository;
    
    @Override
    public User createUser(String name, String email, String phone, String password, Integer state, String remark) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(name)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（如果邮箱不为空）
        if (StringUtil.isNotEmpty(email)) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }
        
        // 使用流式创建用户实体
        User user = User.builder()
                .username(name)
                .email(email)
                .phone(phone)
                .password(password)
                .state(state)
                .remark(remark)
                .build();
        // 保存用户
        return userRepository.save(user);
    }
    
    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public User updateUserEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 检查新邮箱是否已存在（如果邮箱不为空）
        if (StringUtil.isNotEmpty(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }
        
        // 使用流式更新邮箱
        User updatedUser = user.toBuilder()
                .email(newEmail)
                .build();
        
        return userRepository.save(updatedUser);
    }
    
    @Override
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 使用流式激活用户
        User activatedUser = user.toBuilder()
                .state(1)
                .build();
        
        return userRepository.save(activatedUser);
    }
    
    @Override
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 使用流式停用用户
        User deactivatedUser = user.toBuilder()
                .state(2)
                .build();
        
        return userRepository.save(deactivatedUser);
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        return userRepository.delete(userId);
    }
}

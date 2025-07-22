package top.fblue.watermelon.domain.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.domain.user.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.Email;
import top.fblue.watermelon.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * 用户领域服务实现
 */
@Service
public class UserDomainServiceImpl implements UserDomainService {
    
    private final UserRepository userRepository;
    
    @Override
    public User createUser(String name, String email, String phone, String password, Integer state, String remark) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(name)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（如果邮箱不为空）
        if (email != null && !email.trim().isEmpty()) {
            if (userRepository.existsByEmail(new Email(email))) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }
        
        // 创建用户实体
        User user = new User(name, email != null && !email.trim().isEmpty() ? new Email(email) : null, password);
        user.setPhone(phone != null && !phone.trim().isEmpty() ? phone : null);
        user.setState(state);
        user.setRemark(remark);
        
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
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            if (userRepository.existsByEmail(new Email(newEmail))) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }
        
        // 更新邮箱
        user.changeEmail(newEmail != null && !newEmail.trim().isEmpty() ? new Email(newEmail) : null);
        return userRepository.save(user);
    }
    
    @Override
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.activate();
        return userRepository.save(user);
    }
    
    @Override
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.deactivate();
        return userRepository.save(user);
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        return userRepository.delete(userId);
    }
}

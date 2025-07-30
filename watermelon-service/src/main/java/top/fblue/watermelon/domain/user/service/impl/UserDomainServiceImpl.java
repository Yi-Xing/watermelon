package top.fblue.watermelon.domain.user.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.repository.UserRepository;
import top.fblue.watermelon.domain.user.repository.UserRoleRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 用户领域服务实现
 * 专注于聚合间的协调，而不是业务规则校验
 */
@Service
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private UserRepository userRepository;
    
    @Resource
    private UserRoleRepository userRoleRepository;

    @Override
    public User createUser(User user) {
        // 校验业务规则
        validateUser(user);

        // 保存用户
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findByIds(userIds);
    }

    @Override
    public Map<Long, User> getUserMapByIds(List<Long> userIds) {
        List<User> users = this.getUsersByIds(userIds);

        // 转换为Map，方便按ID查找
        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        Function.identity()
                ));
    }

    @Override
    public boolean deleteUser(Long userId) {
        // 1. 检查用户是否存在
        getUserById(userId);
        
        // 2. 删除用户角色关系
        userRoleRepository.deleteByUserId(userId);
        
        // 3. 删除用户
        return userRepository.delete(userId);
    }

    @Override
    public List<User> getUserList(String keyword, Integer state, int pageNum, int pageSize) {
        return userRepository.findByCondition(keyword, state, pageNum, pageSize);
    }

    @Override
    public boolean updateUser(User user) {
        // 校验业务规则
        validateUserUpdate(user);
        
        // 更新用户
        return userRepository.update(user);
    }

    @Override
    public boolean resetPassword(Long userId, String password) {
        // 检查用户是否存在
        getUserById(userId);
        
        // 重设密码
        return userRepository.resetPassword(userId, password);
    }

    @Override
    public Long countUsers(String keyword, Integer state) {
        return userRepository.countByCondition(keyword, state);
    }

    /**
     * 校验用户的业务规则
     */
    private void validateUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果邮箱不为空）
        if (StringUtil.isNotEmpty(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }

        // 检查手机号是否已存在（如果手机号不为空）
        if (StringUtil.isNotEmpty(user.getPhone())) {
            if (userRepository.existsByPhone(user.getPhone())) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }
    }
    
    /**
     * 校验用户更新的业务规则
     */
    private void validateUserUpdate(User user) {
        // 检查用户是否存在
        User existingUser = getUserById(user.getId());
        
        // 检查用户名是否已存在（如果修改了用户名）
        if (!existingUser.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("用户名已存在");
            }
        }

        // 检查邮箱是否已存在（如果修改了邮箱且不为空）
        if (StringUtil.isNotEmpty(user.getEmail()) && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }

        // 检查手机号是否已存在（如果修改了手机号且不为空）
        if (StringUtil.isNotEmpty(user.getPhone()) && !user.getPhone().equals(existingUser.getPhone())) {
            if (userRepository.existsByPhone(user.getPhone())) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }
    }
}

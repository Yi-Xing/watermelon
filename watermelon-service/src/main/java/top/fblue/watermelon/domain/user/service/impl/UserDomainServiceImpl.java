package top.fblue.watermelon.domain.user.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.repository.UserRepository;

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

        // 检查手机号是否已存在（如果手机号不为空）
        if (StringUtil.isNotEmpty(phone)) {
            if (userRepository.existsByPhone(phone)) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }

        // 使用Builder创建用户实体
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
        return userRepository.findByIds(new HashSet<>(userIds));
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
        return userRepository.delete(userId);
    }

    @Override
    public List<User> getUserList(String keyword, Integer state, int pageNum, int pageSize) {
        return userRepository.findByCondition(keyword, state, pageNum, pageSize);
    }

    @Override
    public Long countUsers(String keyword, Integer state) {
        return userRepository.countByCondition(keyword, state);
    }
}

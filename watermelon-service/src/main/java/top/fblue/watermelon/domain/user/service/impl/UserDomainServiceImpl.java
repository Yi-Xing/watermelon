package top.fblue.watermelon.domain.user.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.common.utils.EncryptionUtil;
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

        // 先保存用户获取ID，然后加密密码
        User savedUser = userRepository.save(user);

        // 如果密码不为空，则加密密码
        if (StringUtil.isNotEmpty(user.getPassword())) {
            String encodedPassword = EncryptionUtil.encode(user.getPassword(), savedUser.getId());

            // 更新加密后的密码
            savedUser.setPassword(encodedPassword);
            userRepository.resetPassword(savedUser.getId(), encodedPassword);
        }
        return savedUser;
    }

    @Override
    public User getUserById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
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
    public List<Long> getUserRoles(Long userId) {
        return userRoleRepository.findRoleIdsByUserId(userId);
    }

    @Override
    public void updateUserRole(Long userId, List<Long> roleIds) {
        // 1. 检查用户是否存在
        getUserById(userId);

        // 2. 查询现有的用户角色关系
        List<Long> existingRoleIds = userRoleRepository.findRoleIdsByUserId(userId);

        // 3. 计算需要删除和新增的角色ID
        Set<Long> roleIdSet = new HashSet<>(roleIds);
        List<Long> toDelete = existingRoleIds.stream()
                .filter(id -> !roleIdSet.contains(id))
                .collect(Collectors.toList());

        List<Long> toInsert = roleIdSet.stream()
                .filter(id -> !existingRoleIds.contains(id))
                .collect(Collectors.toList());

        // 4. 批量删除不需要的关系
        if (!toDelete.isEmpty()) {
            userRoleRepository.deleteBatch(userId, toDelete);
        }

        // 5. 批量新增新的关系
        if (!toInsert.isEmpty()) {
            userRoleRepository.insertBatch(userId, toInsert);
        }
    }

    @Override
    public void createUserRole(Long userId, List<Long> roleIds) {
        // 1. 检查用户是否存在
        getUserById(userId);

        // 2. 角色id去重
        roleIds = roleIds.stream()
                .distinct()
                .collect(Collectors.toList());

        // 3. 创建用户角色关系
        userRoleRepository.insertBatch(userId, roleIds);
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

        // 加密密码
        String encodedPassword = EncryptionUtil.encode(password, userId);

        // 重设密码
        return userRepository.resetPassword(userId, encodedPassword);
    }

    @Override
    public Long countUsers(String keyword, Integer state) {
        return userRepository.countByCondition(keyword, state);
    }

    @Override
    public User login(String account, String password) {
        // 根据账号查找用户
        User user = userRepository.findByAccount(account);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码（使用BCrypt验证）
        if (StringUtil.isNotEmpty(user.getPassword()) && !EncryptionUtil.matches(password, user.getPassword(), user.getId())) {
            throw new BusinessException("密码错误");
        }

        // 检查用户状态
        if (!StateEnum.ENABLE.getCode().equals(user.getState())) {
            throw new BusinessException("用户已被禁用");
        }

        return user;
    }

    /**
     * 校验用户的业务规则
     */
    private void validateUser(User user) {
        // 检查邮箱是否已存在（如果邮箱不为空）
        if (StringUtil.isNotEmpty(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 检查手机号是否已存在（如果手机号不为空）
        if (StringUtil.isNotEmpty(user.getPhone())) {
            if (userRepository.existsByPhone(user.getPhone())) {
                throw new BusinessException("手机号已存在");
            }
        }
    }

    /**
     * 校验用户更新的业务规则
     */
    private void validateUserUpdate(User user) {
        // 检查用户是否存在
        User existingUser = getUserById(user.getId());

        // 检查邮箱是否已存在（如果修改了邮箱且不为空）
        if (StringUtil.isNotEmpty(user.getEmail()) && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 检查手机号是否已存在（如果修改了手机号且不为空）
        if (StringUtil.isNotEmpty(user.getPhone()) && !user.getPhone().equals(existingUser.getPhone())) {
            if (userRepository.existsByPhone(user.getPhone())) {
                throw new BusinessException("手机号已存在");
            }
        }
    }
}

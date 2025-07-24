package top.fblue.watermelon.domain.user.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserWithRelatedInfo;
import top.fblue.watermelon.domain.user.entity.UserBasicInfo;
import top.fblue.watermelon.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
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
    
    public User getUserById(Long id) {
       return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
    
    @Override
    public UserWithRelatedInfo getUserWithRelatedInfoById(Long id) {
        return assembleUserWithRelatedInfo(this.getUserById(id));
    }
    
    @Override
    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("手机号对应的用户不存在"));
    }
    
    @Override
    public UserWithRelatedInfo getUserWithRelatedInfoByPhone(String phone) {
        User user = getUserByPhone(phone);
        return assembleUserWithRelatedInfo(user);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public List<UserWithRelatedInfo> getAllUsersWithRelatedInfo() {
        List<User> users = userRepository.findAll();
        return assembleUsersWithRelatedInfo(users);
    }
    
    @Override
    public Map<Long, User> getBatchUsersByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<User> users = userRepository.findByIds(userIds);
        
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
    
    /**
     * 组装单个用户的关联信息
     * Domain层的聚合组装逻辑
     */
    private UserWithRelatedInfo assembleUserWithRelatedInfo(User user) {
        if (user == null) {
            return null;
        }
        
        // 收集需要查询的用户ID
        Set<Long> relatedUserIds = new HashSet<>();
        if (user.getCreatedBy() != null) {
            relatedUserIds.add(user.getCreatedBy());
        }
        if (user.getUpdatedBy() != null) {
            relatedUserIds.add(user.getUpdatedBy());
        }
        
        // 批量查询关联用户信息
        Map<Long, User> relatedUsersMap = getBatchUsersByIds(relatedUserIds);

        // 组装完整的聚合对象
        return UserWithRelatedInfo.builder()
                .user(user)
                .createdByUser(convertToUserBasicInfo(
                    user.getCreatedBy() != null ? relatedUsersMap.get(user.getCreatedBy()) : null
                ))
                .updatedByUser(convertToUserBasicInfo(
                    user.getUpdatedBy() != null ? relatedUsersMap.get(user.getUpdatedBy()) : null
                ))
                .build();
    }
    
    /**
     * 批量组装用户的关联信息
     * 优化性能，避免N+1问题
     */
    private List<UserWithRelatedInfo> assembleUsersWithRelatedInfo(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 收集所有需要查询的关联用户ID
        Set<Long> allRelatedUserIds = new HashSet<>();
        for (User user : users) {
            if (user.getCreatedBy() != null) {
                allRelatedUserIds.add(user.getCreatedBy());
            }
            if (user.getUpdatedBy() != null) {
                allRelatedUserIds.add(user.getUpdatedBy());
            }
        }
        
        // 一次性批量查询所有关联用户
        Map<Long, User> relatedUsersMap = getBatchUsersByIds(allRelatedUserIds);
        
        // 批量组装
        return users.stream()
                .map(user -> UserWithRelatedInfo.builder()
                        .user(user)
                        .createdByUser(convertToUserBasicInfo(
                            user.getCreatedBy() != null ? relatedUsersMap.get(user.getCreatedBy()) : null
                        ))
                        .updatedByUser(convertToUserBasicInfo(
                            user.getUpdatedBy() != null ? relatedUsersMap.get(user.getUpdatedBy()) : null
                        ))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 转换User为UserBasicInfo
     */
    private UserBasicInfo convertToUserBasicInfo(User user) {
        if (user == null) {
            return null;
        }
        return UserBasicInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}

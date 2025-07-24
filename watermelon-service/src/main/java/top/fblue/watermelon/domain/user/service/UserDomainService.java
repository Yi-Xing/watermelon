package top.fblue.watermelon.domain.user.service;

import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserWithRelatedInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 用户领域服务接口
 * 处理跨聚合的业务逻辑
 */
public interface UserDomainService {
    
    /**
     * 创建用户
     */
    User createUser(String name, String email, String phone, String password, Integer state, String remark);
    
    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);
    
    /**
     * 根据ID获取用户（包含关联用户信息）
     */
    UserWithRelatedInfo getUserWithRelatedInfoById(Long id);
    
    /**
     * 根据手机号获取用户
     */
    User getUserByPhone(String phone);
    
    /**
     * 根据手机号获取用户（包含关联用户信息）
     */
    UserWithRelatedInfo getUserWithRelatedInfoByPhone(String phone);
    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 获取所有用户（包含关联用户信息）
     */
    List<UserWithRelatedInfo> getAllUsersWithRelatedInfo();
    
    /**
     * 批量根据用户ID获取用户信息
     * 用于获取创建人、更新人等关联用户信息
     * 
     * @param userIds 用户ID集合
     * @return 用户信息映射表（key: userId, value: User）
     */
    Map<Long, User> getBatchUsersByIds(Set<Long> userIds);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
}

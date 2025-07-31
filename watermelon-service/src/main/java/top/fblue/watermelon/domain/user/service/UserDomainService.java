package top.fblue.watermelon.domain.user.service;

import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.role.entity.Role;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户领域服务接口
 * 处理跨聚合的业务逻辑
 */
public interface UserDomainService {
    
    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 批量根据ID获取用户
     */
    List<User> getUsersByIds(List<Long> userIds);

    /**
     * 批量根据ID获取用户
     */
     Map<Long, User> getUserMapByIds(List<Long> userIdSet);
    /**
     * 分页查询用户列表
     */
    List<User> getUserList(String keyword, Integer state, int pageNum, int pageSize);
    
    /**
     * 统计用户总数
     */
    Long countUsers(String keyword, Integer state);

    /**
     * 更新用户
     */
    boolean updateUser(User user);
    
    /**
     * 重设密码
     */
    boolean resetPassword(Long userId, String password);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
    
    /**
     * 获取用户关联的角色信息
     */
    List<Long> getUserRoles(Long userId);
    
    /**
     * 更新用户角色关系
     */
    void updateUserRole(Long userId, List<Long> roleIds);
}

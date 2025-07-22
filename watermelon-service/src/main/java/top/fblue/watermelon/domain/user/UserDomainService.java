package top.fblue.watermelon.domain.user;

import top.fblue.watermelon.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

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
    Optional<User> getUserById(Long id);
    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 更新用户邮箱
     */
    User updateUserEmail(Long userId, String newEmail);
    
    /**
     * 激活用户
     */
    User activateUser(Long userId);
    
    /**
     * 停用用户
     */
    User deactivateUser(Long userId);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
}

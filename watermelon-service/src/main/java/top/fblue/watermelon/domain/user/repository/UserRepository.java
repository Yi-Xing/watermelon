package top.fblue.watermelon.domain.user.repository;

import top.fblue.watermelon.domain.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    /**
     * 保存用户
     */
    User save(User user);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);
    
    /**
     * 查找所有用户
     */
    List<User> findAll();
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 批量根据ID查找用户
     * 
     * @param userIds 用户ID集合
     * @return 用户列表
     */
    List<User> findByIds(Set<Long> userIds);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 删除用户
     */
    boolean delete(long id);
}

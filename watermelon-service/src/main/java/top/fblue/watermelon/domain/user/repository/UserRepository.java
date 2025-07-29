package top.fblue.watermelon.domain.user.repository;

import top.fblue.watermelon.domain.user.entity.User;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserRepository {

    /**
     * 保存用户
     */
    User save(User user);

    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 批量根据ID查找用户
     *
     * @param userIds 用户ID集合
     * @return 用户列表
     */
    List<User> findByIds(List<Long> userIds);

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

    /**
     * 根据条件分页查询用户列表
     *
     * @param keyword  搜索关键词（用户名、邮箱、手机号模糊匹配）
     * @param state    用户状态
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<User> findByCondition(String keyword, Integer state, int pageNum, int pageSize);

    /**
     * 根据条件统计用户总数
     *
     * @param keyword 搜索关键词（用户名、邮箱、手机号模糊匹配）
     * @param state   用户状态
     * @return 用户总数
     */
    Long countByCondition(String keyword, Integer state);
    
    /**
     * 更新用户
     */
    boolean update(User user);
    
    /**
     * 重设密码
     */
    boolean resetPassword(Long userId, String password);
}

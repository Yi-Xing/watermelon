package top.fblue.watermelon.domain.user.service;

import top.fblue.watermelon.domain.user.entity.User;

import java.util.List;

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
     * 根据ID获取用户详情
     */
    User getUserDetailById(Long id);
    
    /**
     * 分页查询用户列表
     */
    List<User> getUserList(String keyword, Integer state, int offset, int limit);
    
    /**
     * 统计用户总数
     */
    Long countUsers(String keyword, Integer state);

    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
}

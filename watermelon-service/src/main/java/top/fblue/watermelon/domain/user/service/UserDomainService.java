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
     * 根据ID获取用户（包含关联用户信息）
     */
    UserWithRelatedInfo getUserWithRelatedInfoById(Long id);

    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
}

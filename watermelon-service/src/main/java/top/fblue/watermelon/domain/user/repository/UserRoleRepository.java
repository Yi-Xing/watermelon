package top.fblue.watermelon.domain.user.repository;

import java.util.List;

/**
 * 用户角色关系仓储接口
 */
public interface UserRoleRepository {
    
    /**
     * 根据用户ID删除角色关系
     */
    void deleteByUserId(Long userId);
    
    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 根据角色 ID 查询关联用户 ID 列表。
     *
     * @param roleId 角色 ID
     * @return 关联用户 ID 列表
     */
    List<Long> findUserIdsByRoleId(Long roleId);
    
    /**
     * 批量删除用户角色关系
     */
    void deleteBatch(Long userId, List<Long> roleIds);
    
    /**
     * 批量新增用户角色关系
     */
    void insertBatch(Long userId, List<Long> roleIds);
}

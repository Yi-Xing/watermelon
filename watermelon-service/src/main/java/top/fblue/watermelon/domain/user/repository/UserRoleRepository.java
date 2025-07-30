package top.fblue.watermelon.domain.user.repository;

import java.util.List;

/**
 * 用户角色关系仓储接口
 */
public interface UserRoleRepository {
    
    /**
     * 根据用户ID删除角色关系
     */
    boolean deleteByUserId(Long userId);
    
    /**
     * 批量保存用户角色关系
     */
    void saveBatch(List<Long> userId, List<Long> roleIds);
    
    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> findRoleIdsByUserId(Long userId);
    
    /**
     * 批量删除用户角色关系
     */
    void deleteBatch(Long userId, List<Long> roleIds);
    
    /**
     * 批量新增用户角色关系
     */
    void insertBatch(Long userId, List<Long> roleIds);
}

package top.fblue.watermelon.domain.role.repository;

import java.util.List;

/**
 * 角色资源关系仓储接口
 */
public interface RoleResourceRepository {
    
    /**
     * 根据角色ID删除资源关系
     */
    boolean deleteByRoleId(Long roleId);

    /**
     * 根据角色ID查询资源ID列表
     */
    List<Long> findResourceIdsByRoleId(Long roleId);
    
    /**
     * 批量删除角色资源关系
     */
    void deleteBatch(Long roleId, List<Long> resourceIds);
    
    /**
     * 批量新增角色资源关系
     */
    void insertBatch(Long roleId, List<Long> resourceIds);
}

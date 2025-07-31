package top.fblue.watermelon.domain.role.repository;

import top.fblue.watermelon.domain.role.entity.Role;
import java.util.List;

/**
 * 角色仓储接口
 */
public interface RoleRepository {
    
    /**
     * 保存角色
     */
    Role save(Role role);
    
    /**
     * 根据ID查找角色
     */
    Role findById(Long id);
    
    /**
     * 根据IDs批量查找角色
     */
    List<Role> findByIds(List<Long> ids);
    
    /**
     * 根据条件分页查询角色列表
     */
    List<Role> findByCondition(String name, Integer state, int pageNum, int pageSize);
    
    /**
     * 根据条件统计角色总数
     */
    Long countByCondition(String name, Integer state);
    
    /**
     * 更新角色
     */
    boolean update(Role role);
    
    /**
     * 删除角色
     */
    boolean delete(Long id);
    
    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);
} 
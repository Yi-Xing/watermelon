package top.fblue.watermelon.domain.role.service;

import top.fblue.watermelon.domain.role.entity.Role;
import java.util.List;

/**
 * 角色领域服务接口
 */
public interface RoleDomainService {
    
    /**
     * 创建角色
     */
    Role createRole(Role role);
    
    /**
     * 根据ID获取角色
     */
    Role getRoleById(Long id);

    /**
     * 根据IDs获取角色
     */
    List<Role> getRoleByIds(List<Long> ids);

    /**
     * 校验角色ID是否存在
     */
    void validateRoleIds(List<Long> roleIds);

    /**
     * 分页查询角色列表
     */
    List<Role> getRoleList(String name, Integer state, int pageNum, int pageSize);
    
    /**
     * 统计角色总数
     */
    Long countRoles(String name, Integer state);
    
    /**
     * 更新角色
     */
    boolean updateRole(Role role);
    
    /**
     * 更新角色资源
     */
    boolean updateRoleResource(Long roleId, List<Long> resourceIds);
    
    /**
     * 获取角色关联的资源ID列表
     */
    List<Long> getRoleResourceIds(Long roleId);
    
    /**
     * 删除角色
     */
    boolean deleteRole(Long id);
    
    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);
} 
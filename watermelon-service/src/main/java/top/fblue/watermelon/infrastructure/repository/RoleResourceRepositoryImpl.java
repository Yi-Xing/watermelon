package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.infrastructure.converter.RoleResourcePOConverter;
import top.fblue.watermelon.infrastructure.mapper.RoleResourceMapper;
import top.fblue.watermelon.infrastructure.po.RoleResourcePO;

import java.util.List;

/**
 * 角色资源关系仓储实现
 */
@Repository
public class RoleResourceRepositoryImpl {

    @Resource
    private RoleResourceMapper roleResourceMapper;
    @Resource
    private RoleResourcePOConverter roleResourcePOConverter;

    /**
     * 保存角色资源关系
     */
    public RoleResourcePO save(RoleResourcePO roleResource) {
        if (roleResource.getId() == null) {
            roleResourceMapper.insert(roleResource);
        } else {
            roleResourceMapper.updateById(roleResource);
        }
        return roleResourceMapper.selectById(roleResource.getId());
    }

    /**
     * 批量保存角色资源关系
     */
    public void saveBatch(List<RoleResourcePO> roleResources) {
        for (RoleResourcePO roleResource : roleResources) {
            roleResourceMapper.insert(roleResource);
        }
    }

    /**
     * 根据角色ID删除资源关系
     */
    public boolean deleteByRoleId(Long roleId) {
        QueryWrapper<RoleResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return roleResourceMapper.delete(queryWrapper) > 0;
    }

    /**
     * 根据资源ID删除角色关系
     */
    public boolean deleteByResourceId(Long resourceId) {
        QueryWrapper<RoleResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        return roleResourceMapper.delete(queryWrapper) > 0;
    }

    /**
     * 删除指定的角色资源关系
     */
    public boolean deleteByRoleIdAndResourceId(Long roleId, Long resourceId) {
        QueryWrapper<RoleResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId).eq("resource_id", resourceId);
        return roleResourceMapper.delete(queryWrapper) > 0;
    }

    /**
     * 根据角色ID查询资源ID列表
     */
    public List<Long> findResourceIdsByRoleId(Long roleId) {
        return roleResourceMapper.selectResourceIdsByRoleId(roleId);
    }

    /**
     * 根据资源ID查询角色ID列表
     */
    public List<Long> findRoleIdsByResourceId(Long resourceId) {
        return roleResourceMapper.selectRoleIdsByResourceId(resourceId);
    }

    /**
     * 根据角色ID列表查询资源ID列表
     */
    public List<Long> findResourceIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleResourceMapper.selectResourceIdsByRoleIds(roleIds);
    }

    /**
     * 检查角色资源关系是否存在
     */
    public boolean existsByRoleIdAndResourceId(Long roleId, Long resourceId) {
        QueryWrapper<RoleResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId).eq("resource_id", resourceId);
        return roleResourceMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 根据角色ID查询角色资源关系列表
     */
    public List<RoleResourcePO> findByRoleId(Long roleId) {
        QueryWrapper<RoleResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return roleResourceMapper.selectList(queryWrapper);
    }

    /**
     * 根据资源ID查询角色资源关系列表
     */
    public List<RoleResourcePO> findByResourceId(Long resourceId) {
        QueryWrapper<RoleResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        return roleResourceMapper.selectList(queryWrapper);
    }
} 
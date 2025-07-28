package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.infrastructure.converter.RoleResourceNodePOConverter;
import top.fblue.watermelon.infrastructure.mapper.RoleResourceNodeMapper;
import top.fblue.watermelon.infrastructure.po.RoleResourceNodePO;

import java.util.List;

/**
 * 角色资源关系仓储实现
 */
@Repository
public class RoleResourceNodeRepositoryImpl {

    @Resource
    private RoleResourceNodeMapper roleResourceNodeMapper;
    @Resource
    private RoleResourceNodePOConverter roleResourceNodePOConverter;

    /**
     * 保存角色资源关系
     */
    public RoleResourceNodePO save(RoleResourceNodePO roleResource) {
        if (roleResource.getId() == null) {
            roleResourceNodeMapper.insert(roleResource);
        } else {
            roleResourceNodeMapper.updateById(roleResource);
        }
        return roleResourceNodeMapper.selectById(roleResource.getId());
    }

    /**
     * 批量保存角色资源关系
     */
    public void saveBatch(List<RoleResourceNodePO> roleResources) {
        for (RoleResourceNodePO roleResource : roleResources) {
            roleResourceNodeMapper.insert(roleResource);
        }
    }

    /**
     * 根据角色ID删除资源关系
     */
    public boolean deleteByRoleId(Long roleId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return roleResourceNodeMapper.delete(queryWrapper) > 0;
    }

    /**
     * 根据资源ID删除角色关系
     */
    public boolean deleteByResourceId(Long resourceId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        return roleResourceNodeMapper.delete(queryWrapper) > 0;
    }

    /**
     * 删除指定的角色资源关系
     */
    public boolean deleteByRoleIdAndResourceId(Long roleId, Long resourceId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId).eq("resource_id", resourceId);
        return roleResourceNodeMapper.delete(queryWrapper) > 0;
    }

    /**
     * 根据角色ID查询资源ID列表
     */
    public List<Long> findResourceIdsByRoleId(Long roleId) {
        return roleResourceNodeMapper.selectResourceIdsByRoleId(roleId);
    }

    /**
     * 根据资源ID查询角色ID列表
     */
    public List<Long> findRoleIdsByResourceId(Long resourceId) {
        return roleResourceNodeMapper.selectRoleIdsByResourceId(resourceId);
    }

    /**
     * 根据角色ID列表查询资源ID列表
     */
    public List<Long> findResourceIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleResourceNodeMapper.selectResourceIdsByRoleIds(roleIds);
    }

    /**
     * 检查角色资源关系是否存在
     */
    public boolean existsByRoleIdAndResourceId(Long roleId, Long resourceId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId).eq("resource_id", resourceId);
        return roleResourceNodeMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 根据角色ID查询角色资源关系列表
     */
    public List<RoleResourceNodePO> findByRoleId(Long roleId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return roleResourceNodeMapper.selectList(queryWrapper);
    }

    /**
     * 根据资源ID查询角色资源关系列表
     */
    public List<RoleResourceNodePO> findByResourceId(Long resourceId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        return roleResourceNodeMapper.selectList(queryWrapper);
    }
} 
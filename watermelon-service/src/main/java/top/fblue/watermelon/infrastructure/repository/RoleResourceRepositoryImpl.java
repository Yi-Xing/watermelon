package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.role.repository.RoleResourceRepository;
import top.fblue.watermelon.infrastructure.converter.RoleResourceNodePOConverter;
import top.fblue.watermelon.infrastructure.mapper.RoleResourceNodeMapper;
import top.fblue.watermelon.infrastructure.po.RoleResourceNodePO;

import java.util.List;
import java.util.ArrayList;

/**
 * 角色资源关系仓储实现
 */
@Repository
public class RoleResourceRepositoryImpl implements RoleResourceRepository {

    @Resource
    private RoleResourceNodeMapper roleResourceNodeMapper;
    @Resource
    private RoleResourceNodePOConverter roleResourceNodePOConverter;

    @Override
    public boolean deleteByRoleId(Long roleId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        
        // 使用 MyBatis-Plus 的逻辑删除
        return roleResourceNodeMapper.delete(queryWrapper) > 0;
    }

    @Override
    public void saveBatch(List<Long> roleIds, List<Long> resourceIds) {
        // 构建批量插入的实体列表
        List<RoleResourceNodePO> entities = new ArrayList<>();
        for (Long roleId : roleIds) {
            for (Long resourceId : resourceIds) {
                RoleResourceNodePO entity = new RoleResourceNodePO();
                entity.setRoleId(roleId);
                entity.setResourceNodeId(resourceId);
                entity.setCreatedBy(1L); // TODO: 应该从当前用户上下文获取真实的用户ID
                entity.setUpdatedBy(1L); // TODO: 应该从当前用户上下文获取真实的用户ID
                entity.setIsDeleted(0);
                entities.add(entity);
            }
        }
        
        // 使用 MyBatis-Plus 的批量插入
        for (RoleResourceNodePO entity : entities) {
            roleResourceNodeMapper.insert(entity);
        }
    }

    @Override
    public List<Long> findResourceIdsByRoleId(Long roleId) {
        return roleResourceNodeMapper.selectResourceIdsByRoleId(roleId);
    }
    
    @Override
    public void deleteBatch(Long roleId, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        queryWrapper.in("resource_node_id", resourceIds);
        
        roleResourceNodeMapper.delete(queryWrapper);
    }
    
    @Override
    public void insertBatch(Long roleId, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }

        // 使用转换器转换为PO列表
        List<RoleResourceNodePO> entities = roleResourceNodePOConverter.toPOList(roleId, resourceIds);

        roleResourceNodeMapper.insert(entities);
    }
} 
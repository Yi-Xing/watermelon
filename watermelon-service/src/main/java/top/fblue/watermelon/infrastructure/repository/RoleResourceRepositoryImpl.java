package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.role.repository.RoleResourceRepository;
import top.fblue.watermelon.infrastructure.converter.RoleResourceNodePOConverter;
import top.fblue.watermelon.infrastructure.mapper.RoleResourceNodeMapper;
import top.fblue.watermelon.infrastructure.po.RoleResourceNodePO;

import java.util.List;

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
    public void deleteByRoleId(Long roleId) {
        QueryWrapper<RoleResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);

        roleResourceNodeMapper.delete(queryWrapper);
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
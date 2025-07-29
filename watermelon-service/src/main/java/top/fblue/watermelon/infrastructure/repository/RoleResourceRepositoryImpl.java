package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.role.repository.RoleResourceRepository;
import top.fblue.watermelon.infrastructure.mapper.ResourceNodeMapper;

import java.util.List;

/**
 * 角色资源关系仓储实现
 */
@Repository
public class RoleResourceRepositoryImpl implements RoleResourceRepository {

    @Resource
    private ResourceNodeMapper resourceNodeMapper;

    @Override
    public boolean deleteByRoleId(Long roleId) {
        return resourceNodeMapper.deleteByRoleId(roleId) > 0;
    }

    @Override
    public void saveBatch(List<Long> roleIds, List<Long> resourceIds) {
        // 直接执行SQL插入角色资源关系
        for (Long roleId : roleIds) {
            for (Long resourceId : resourceIds) {
                // 使用原生SQL插入，这里暂时使用1作为创建人和更新人
                // TODO: 应该从当前用户上下文获取真实的用户ID
                resourceNodeMapper.insertRoleResource(roleId, resourceId, 1L, 1L);
            }
        }
    }

    @Override
    public List<Long> findResourceIdsByRoleId(Long roleId) {
        return resourceNodeMapper.selectResourceIdsByRoleId(roleId);
    }
} 
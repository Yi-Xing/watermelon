package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.user.repository.UserRoleRepository;
import top.fblue.watermelon.infrastructure.mapper.UserRoleMapper;
import top.fblue.watermelon.infrastructure.po.UserRolePO;

import java.util.List;

/**
 * 用户角色关系仓储实现
 */
@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public boolean deleteByUserId(Long userId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userRoleMapper.delete(queryWrapper) > 0;
    }

    @Override
    public void saveBatch(List<Long> userIds, List<Long> roleIds) {
        for (Long userId : userIds) {
            for (Long roleId : roleIds) {
                UserRolePO po = UserRolePO.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build();
                userRoleMapper.insert(po);
            }
        }
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }
    
    @Override
    public void deleteBatch(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.in("role_id", roleIds);
        
        // 使用 MyBatis-Plus 的逻辑删除
        userRoleMapper.delete(queryWrapper);
    }
    
    @Override
    public void insertBatch(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        
        for (Long roleId : roleIds) {
            UserRolePO po = UserRolePO.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .build();
            userRoleMapper.insert(po);
        }
    }
} 
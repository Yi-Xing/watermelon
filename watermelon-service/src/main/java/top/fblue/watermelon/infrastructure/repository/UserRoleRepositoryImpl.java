package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.infrastructure.converter.UserRolePOConverter;
import top.fblue.watermelon.infrastructure.mapper.UserRoleMapper;
import top.fblue.watermelon.infrastructure.po.UserRolePO;

import java.util.List;

/**
 * 用户角色关系仓储实现
 */
@Repository
public class UserRoleRepositoryImpl {

    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRolePOConverter userRolePOConverter;

    /**
     * 保存用户角色关系
     */
    public UserRolePO save(UserRolePO userRole) {
        if (userRole.getId() == null) {
            userRoleMapper.insert(userRole);
        } else {
            userRoleMapper.updateById(userRole);
        }
        return userRoleMapper.selectById(userRole.getId());
    }

    /**
     * 批量保存用户角色关系
     */
    public void saveBatch(List<UserRolePO> userRoles) {
        for (UserRolePO userRole : userRoles) {
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 根据用户ID删除角色关系
     */
    public boolean deleteByUserId(Long userId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userRoleMapper.delete(queryWrapper) > 0;
    }

    /**
     * 根据角色ID删除用户关系
     */
    public boolean deleteByRoleId(Long roleId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return userRoleMapper.delete(queryWrapper) > 0;
    }

    /**
     * 删除指定的用户角色关系
     */
    public boolean deleteByUserIdAndRoleId(Long userId, Long roleId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_id", roleId);
        return userRoleMapper.delete(queryWrapper) > 0;
    }

    /**
     * 根据用户ID查询角色ID列表
     */
    public List<Long> findRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    /**
     * 根据角色ID查询用户ID列表
     */
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return userRoleMapper.selectUserIdsByRoleId(roleId);
    }

    /**
     * 检查用户角色关系是否存在
     */
    public boolean existsByUserIdAndRoleId(Long userId, Long roleId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_id", roleId);
        return userRoleMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 根据用户ID查询用户角色关系列表
     */
    public List<UserRolePO> findByUserId(Long userId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userRoleMapper.selectList(queryWrapper);
    }

    /**
     * 根据角色ID查询用户角色关系列表
     */
    public List<UserRolePO> findByRoleId(Long roleId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return userRoleMapper.selectList(queryWrapper);
    }
} 
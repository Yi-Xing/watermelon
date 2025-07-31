package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.user.repository.UserRoleRepository;
import top.fblue.watermelon.infrastructure.converter.UserRolePOConverter;
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
    @Resource
    private UserRolePOConverter userRolePOConverter;

    @Override
    public void deleteByUserId(Long userId) {
        QueryWrapper<UserRolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        userRoleMapper.delete(queryWrapper);
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

        userRoleMapper.delete(queryWrapper);
    }

    @Override
    public void insertBatch(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        // 使用转换器转换为PO列表
        List<UserRolePO> poList = userRolePOConverter.toPOList(userId, roleIds);

        userRoleMapper.insert(poList);
    }
} 
package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.repository.UserRepository;
import top.fblue.watermelon.infrastructure.converter.UserPOConverter;
import top.fblue.watermelon.infrastructure.mapper.UserMapper;
import top.fblue.watermelon.infrastructure.po.UserPO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户仓储实现
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserPOConverter userPOConverter;

    @Override
    public User save(User user) {
        UserPO po = userPOConverter.toPO(user);
        if (po.getId() == null) {
            userMapper.insert(po);
        } else {
            userMapper.updateById(po);
        }
        // 重新查询以获取完整的审计信息
        UserPO savedPO = userMapper.selectById(po.getId());
        return userPOConverter.toDomain(savedPO);
    }

    @Override
    public User findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return userPOConverter.toDomain(po);
    }

    @Override
    public List<User> findByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用MyBatis-Plus的selectByIds方法进行批量查询
        // 这会生成 SELECT * FROM user WHERE id IN (?, ?, ?) 的SQL
        List<UserPO> pos = userMapper.selectByIds(userIds);

        return pos.stream()
                .map(userPOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<User> findByCondition(String keyword, Integer state, int pageNum, int pageSize) {
        QueryWrapper<UserPO> queryWrapper = buildQueryWrapper(keyword, state);
        queryWrapper.orderByDesc("updated_time");
        
        // 使用MyBatis Plus的分页查询
        Page<UserPO> page = new Page<>(pageNum, pageSize);
        IPage<UserPO> pageResult = userMapper.selectPage(page, queryWrapper);
        
        return pageResult.getRecords().stream()
                .map(userPOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByCondition(String keyword, Integer state) {
        QueryWrapper<UserPO> queryWrapper = buildQueryWrapper(keyword, state);
        return userMapper.selectCount(queryWrapper);
    }
    
    /**
     * 构建查询条件
     */
    private QueryWrapper<UserPO> buildQueryWrapper(String keyword, Integer state) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        
        // 关键词模糊查询（用户名、邮箱、手机号）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("email", keyword)
                    .or()
                    .like("phone", keyword)
            );
        }
        
        // 状态筛选
        if (state != null) {
            queryWrapper.eq("state", state);
        }
        
        return queryWrapper;
    }
} 
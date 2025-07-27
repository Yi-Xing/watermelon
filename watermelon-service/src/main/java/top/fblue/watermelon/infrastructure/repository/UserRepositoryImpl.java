package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
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
    public Optional<User> findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return Optional.ofNullable(userPOConverter.toDomain(po));
    }

    @Override
    public List<User> findByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用MyBatis-Plus的selectBatchIds方法进行批量查询
        // 这会生成 SELECT * FROM user WHERE id IN (?, ?, ?) 的SQL
        List<UserPO> pos = userMapper.selectBatchIds(userIds);

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
} 
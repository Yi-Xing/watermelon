package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.repository.UserRepository;
import top.fblue.watermelon.infrastructure.converter.UserConverter;
import top.fblue.watermelon.infrastructure.mapper.UserMapper;
import top.fblue.watermelon.infrastructure.po.UserPO;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserConverter userConverter;
    
    @Override
    public User save(User user) {
        UserPO po = userConverter.toPO(user);
        if (po.getId() == null) {
            // 插入新用户
            userMapper.insert(po);
        } else {
            // 更新现有用户
            userMapper.updateById(po);
        }
        return userConverter.toDomain(po);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return Optional.ofNullable(userConverter.toDomain(po));
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UserPO po = userMapper.selectOne(queryWrapper);
        return Optional.ofNullable(userConverter.toDomain(po));
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", username);
        UserPO po = userMapper.selectOne(queryWrapper);
        return Optional.ofNullable(userConverter.toDomain(po));
    }
    
    @Override
    public List<User> findAll() {
        List<UserPO> pos = userMapper.selectList(null);
        return pos.stream()
                .map(userConverter::toDomain)
                .collect(java.util.stream.Collectors.toList());
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
    public boolean existsByUsername(String username) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }
} 
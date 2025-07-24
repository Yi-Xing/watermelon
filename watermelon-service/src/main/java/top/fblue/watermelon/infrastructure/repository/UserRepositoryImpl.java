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
import java.util.Set;
import java.util.stream.Collectors;

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
            userMapper.insert(po);
        } else {
            userMapper.updateById(po);
        }
        // 重新查询以获取完整的审计信息
        UserPO savedPO = userMapper.selectById(po.getId());
        return userConverter.toDomain(savedPO);
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
    public Optional<User> findByPhone(String phone) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
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
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> findByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 使用MyBatis-Plus的selectBatchIds方法进行批量查询
        // 这会生成 SELECT * FROM user WHERE id IN (?, ?, ?) 的SQL
        List<UserPO> pos = userMapper.selectBatchIds(userIds);
        
        return pos.stream()
                .map(userConverter::toDomain)
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
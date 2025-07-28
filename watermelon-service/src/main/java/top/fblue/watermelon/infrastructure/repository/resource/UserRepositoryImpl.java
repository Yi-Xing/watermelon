package top.fblue.watermelon.infrastructure.repository.resource;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.resource.entity.UserBasicInfo;
import top.fblue.watermelon.domain.resource.repository.UserRepository;
import top.fblue.watermelon.infrastructure.repository.resource.converter.UserPOConverter;
import top.fblue.watermelon.infrastructure.mapper.UserMapper;
import top.fblue.watermelon.infrastructure.po.UserPO;

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
    public UserBasicInfo findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return userPOConverter.toDomain(po);
    }
}
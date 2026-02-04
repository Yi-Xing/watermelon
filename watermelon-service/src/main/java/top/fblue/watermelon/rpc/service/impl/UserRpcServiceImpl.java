package top.fblue.watermelon.rpc.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.api.response.UserBaseResponse;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.rpc.converter.UserConverter;
import top.fblue.watermelon.rpc.service.UserRpcService;

import java.util.List;

/**
 * Dubbo 层用户服务实现，调用 domain.service，使用 dubbo/converter 转换
 */
@Service
public class UserRpcServiceImpl implements UserRpcService {

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private UserConverter userConverter;

    @Override
    public UserBaseResponse getUserBase(Long userId) {
        User user = userDomainService.getUserById(userId);
        return userConverter.toUserBaseResponse(user);
    }

    @Override
    public List<UserBaseResponse> getUserBaseList(List<Long> userIdList) {
        List<User> users = userDomainService.getUsersByIds(userIdList);
        return userConverter.toUserBaseResponseList(users);
    }
}

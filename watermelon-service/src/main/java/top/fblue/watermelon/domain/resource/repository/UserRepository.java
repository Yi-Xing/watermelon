package top.fblue.watermelon.domain.resource.repository;

import top.fblue.watermelon.domain.resource.entity.UserBasicInfo;
import top.fblue.watermelon.domain.user.entity.User;

public interface UserRepository {
    /**
     * 根据ID查找用户
     */
    UserBasicInfo findById(Long id);
}

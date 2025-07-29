package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.DateTimeUtil;

/**
 * 用户转换器
 * Application层的转换器，负责Domain对象到VO的转换
 */
@Component
public class UserConverter {

    /**
     * User转换为UserVO（包含关联用户信息）
     */
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }

        return UserVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .state(user.getState())
                .stateDesc(StateEnum.fromCode(user.getState()).getDesc())
                .remark(user.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(user.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(user.getUpdatedTime()))
                .build();
    }

}
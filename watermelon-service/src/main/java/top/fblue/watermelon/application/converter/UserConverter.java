package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.DateTimeUtil;

import java.util.Map;

/**
 * 用户转换器
 * Application层的转换器，负责Domain对象到VO的转换
 */
@Component
public class UserConverter {

    /**
     * User转换为UserVO
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

    /**
     * User转换为UserVO（包含关联用户信息）
     */
    public UserVO toVO(User user, Map<Long, User> userIDMap) {
        if (user == null) {
            return null;
        }
        User createdBy = userIDMap.get(user.getCreatedBy());
        UserInfoVO createdByInfo= null;
        if (createdBy != null) {
            createdByInfo = UserInfoVO.builder()
                    .id(createdBy.getId())
                    .name(createdBy.getUsername())
                    .build();
        }

        User updatedBy = userIDMap.get(user.getUpdatedBy());
        UserInfoVO updatedByInfo= null;
        if (updatedBy != null) {
            updatedByInfo = UserInfoVO.builder()
                    .id(updatedBy.getId())
                    .name(updatedBy.getUsername())
                    .build();
        }

        return UserVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .state(user.getState())
                .stateDesc(StateEnum.fromCode(user.getState()).getDesc())
                .remark(user.getRemark())
                .createdBy(createdByInfo)
                .createdTime(DateTimeUtil.formatDateTime(user.getCreatedTime()))
                .updatedBy(updatedByInfo)
                .updatedTime(DateTimeUtil.formatDateTime(user.getUpdatedTime()))
                .build();
    }

}
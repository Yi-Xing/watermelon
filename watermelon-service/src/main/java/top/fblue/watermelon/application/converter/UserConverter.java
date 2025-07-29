package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UpdateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
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
    public UserVO toVO(User user, Map<Long, User> userMap) {
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
                .createdBy(convertToUserInfoVO(userMap.get(user.getCreatedBy())))
                .createdTime(DateTimeUtil.formatDateTime(user.getCreatedTime()))
                .updatedBy(convertToUserInfoVO(userMap.get(user.getUpdatedBy())))
                .updatedTime(DateTimeUtil.formatDateTime(user.getUpdatedTime()))
                .build();
    }

    public User toUser(CreateUserDTO dto) {
        if (dto == null) {
            return null;
        }

        // 设置默认值，避免数据库存储null
        String phone = StringUtil.getNonEmptyString(dto.getPhone());
        String email = StringUtil.getNonEmptyString(dto.getEmail());
        String password = StringUtil.getNonEmptyString(dto.getPassword());

        return User.builder()
                .username(dto.getName())
                .email(email)
                .phone(phone)
                .password(password)
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }
    
    public User toUser(UpdateUserDTO dto) {
        if (dto == null) {
            return null;
        }

        // 设置默认值，避免数据库存储null
        String phone = StringUtil.getNonEmptyString(dto.getPhone());
        String email = StringUtil.getNonEmptyString(dto.getEmail());

        return User.builder()
                .id(dto.getId())
                .username(dto.getName())
                .email(email)
                .phone(phone)
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }

    /**
     * User转换为UserInfoVO
     */
    private UserInfoVO convertToUserInfoVO(User user) {
        if (user == null) {
            return null;
        }

        return UserInfoVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }
}
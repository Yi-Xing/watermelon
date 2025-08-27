package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UpdateUserDTO;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.vo.UserBaseVO;
import top.fblue.watermelon.application.vo.RoleInfoVO;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.common.utils.DateTimeUtil;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
     * User转换为UserVO（包含关联用户信息和角色信息）
     */
    public UserVO toVO(User user, Map<Long, User> userMap, List<Role> roles) {
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
                .roles(convertToRoleInfoVOList(roles))
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
    private UserBaseVO convertToUserInfoVO(User user) {
        if (user == null) {
            return null;
        }

        return UserBaseVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }

    /**
     * Role列表转换为RoleInfoVO列表
     */
    private List<RoleInfoVO> convertToRoleInfoVOList(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        return roles.stream()
                .map(this::convertToRoleInfoVO)
                .collect(Collectors.toList());
    }

    /**
     * Role转换为RoleInfoVO
     */
    private RoleInfoVO convertToRoleInfoVO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleInfoVO.builder()
                .id(role.getId())
                .name(role.getName())
                .state(role.getState())
                .stateDesc(StateEnum.fromCode(role.getState()).getDesc())
                .build();
    }

    /**
     * User转换为UserVO
     */
    public CurrentUserVO toVO(User user, UserTokenDTO userToken, List<ResourceNode> resourcesList, String codePrefix) {
        if (user == null) {
            return null;
        }
        // 处理资源列表，分离页面和按钮，并删除code的系统前缀
        List<String> pageCodeList = new ArrayList<>();
        List<String> buttonCodeList = new ArrayList<>();

        codePrefix += ":";
        for (ResourceNode resource : resourcesList) {
            // 删除 "watermelon:" 前缀
            String codeWithoutPrefix = resource.getCode().substring(codePrefix.length());
            if (ResourceTypeEnum.PAGE.getCode().equals(resource.getType())){
                pageCodeList.add(codeWithoutPrefix);
            } else if (ResourceTypeEnum.BUTTON.getCode().equals(resource.getType())){
                buttonCodeList.add(codeWithoutPrefix);
            }
        }

        return CurrentUserVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .remark(user.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(user.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(user.getUpdatedTime()))
                .expireTime(DateTimeUtil.formatDateTime(userToken.getExpireTime()))
                .pageCodeList(pageCodeList)
                .buttonCodeList(buttonCodeList)
                .build();
    }

}
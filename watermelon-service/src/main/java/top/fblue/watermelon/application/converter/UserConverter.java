package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UpdateUserDTO;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.vo.UserBaseVO;
import top.fblue.watermelon.application.vo.RoleInfoVO;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.common.utils.DateTimeUtil;
import top.fblue.watermelon.domain.user.entity.UserToken;

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
     * 将用户领域实体转换为用户视图。
     *
     * @param user 用户领域实体
     * @return 用户视图；用户为空时返回 {@code null}
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
     * 将用户、令牌和权限快照转换为当前用户前端视图。
     *
     * <p>只输出页面和按钮权限，接口权限仅供后端鉴权使用。</p>
     *
     * @param user 用户领域实体
     * @param userToken 当前登录令牌信息
     * @param permissionSnapshot 当前系统权限快照
     * @return 当前用户前端视图；用户为空时返回 {@code null}
     */
    public CurrentUserVO toVO(User user,
                              UserTokenDTO userToken,
                              PermissionSnapshot permissionSnapshot) {
        if (user == null) {
            return null;
        }

        // 1. 删除页面和按钮权限编码中的系统前缀
        List<String> pageCodeList = removeSystemPrefix(
                permissionSnapshot.codesOfType(PermissionResourceTypeEnum.PAGE), permissionSnapshot.systemCode());
        List<String> buttonCodeList = removeSystemPrefix(
                permissionSnapshot.codesOfType(PermissionResourceTypeEnum.BUTTON), permissionSnapshot.systemCode());

        // 2. 组装当前用户前端视图；接口权限不进入前端响应
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

    /**
     * 删除权限编码中的系统前缀。
     *
     * @param resourceCodes 完整资源编码列表
     * @param systemCode 系统编码
     * @return 供当前系统前端使用的资源编码列表
     */
    private List<String> removeSystemPrefix(List<String> resourceCodes, String systemCode) {
        if (resourceCodes == null || resourceCodes.isEmpty()) {
            return List.of();
        }
        String prefix = systemCode + ":";
        return resourceCodes.stream()
                .map(code -> code.startsWith(prefix) ? code.substring(prefix.length()) : code)
                .toList();
    }

    /**
     * User转换为UserVO
     */
    public UserTokenDTO toDto(UserToken userToken) {
        if (userToken == null) {
            return null;
        }
        // 构建用户Token DTO
        return UserTokenDTO
                .builder()
                .userId(userToken.getUserId())
                .token(userToken.getToken())
                .createdTime(userToken.getCreatedTime())
                .expireTime(userToken.getExpireTime())
                .build();
    }
}

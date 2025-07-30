package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleDTO;
import top.fblue.watermelon.application.vo.RoleVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.DateTimeUtil;
import top.fblue.watermelon.common.enums.StateEnum;

import java.util.Map;
import java.util.List;

/**
 * 角色转换器
 */
@Component
public class RoleConverter {

    /**
     * CreateRoleDTO转换为Role
     */
    public Role toRole(CreateRoleDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Role.builder()
                .name(dto.getName())
                .orderNum(dto.getOrderNum())
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }
    
    /**
     * UpdateRoleDTO转换为Role
     */
    public Role toRole(UpdateRoleDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Role.builder()
                .id(dto.getId())
                .name(dto.getName())
                .orderNum(dto.getOrderNum())
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }
    /**
     * Role转换为RoleVO
     */
    public RoleVO toVO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleVO.builder()
                .id(role.getId())
                .name(role.getName())
                .orderNum(role.getOrderNum())
                .state(role.getState())
                .stateDesc(StateEnum.getDescByCode(role.getState()))
                .remark(role.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(role.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(role.getUpdatedTime()))
                .build();
    }

    /**
     * Role转换为RoleVO
     */
    public RoleVO toVO(Role role, Map<Long, User> userMap) {
        if (role == null) {
            return null;
        }
        
        return RoleVO.builder()
                .id(role.getId())
                .name(role.getName())
                .orderNum(role.getOrderNum())
                .state(role.getState())
                .stateDesc(StateEnum.getDescByCode(role.getState()))
                .remark(role.getRemark())
                .createdBy(toUserInfoVO(userMap.get(role.getCreatedBy())))
                .createdTime(DateTimeUtil.formatDateTime(role.getCreatedTime()))
                .updatedBy(toUserInfoVO(userMap.get(role.getUpdatedBy())))
                .updatedTime(DateTimeUtil.formatDateTime(role.getUpdatedTime()))
                .build();
    }
    
    /**
     * Role转换为RoleVO（包含资源ID）
     */
    public RoleVO toVO(Role role, Map<Long, User> userMap, List<Long> resourceIds) {
        if (role == null) {
            return null;
        }
        
        return RoleVO.builder()
                .id(role.getId())
                .name(role.getName())
                .orderNum(role.getOrderNum())
                .state(role.getState())
                .stateDesc(StateEnum.getDescByCode(role.getState()))
                .remark(role.getRemark())
                .createdBy(toUserInfoVO(userMap.get(role.getCreatedBy())))
                .createdTime(DateTimeUtil.formatDateTime(role.getCreatedTime()))
                .updatedBy(toUserInfoVO(userMap.get(role.getUpdatedBy())))
                .updatedTime(DateTimeUtil.formatDateTime(role.getUpdatedTime()))
                .resourceIds(resourceIds)
                .build();
    }
    
    /**
     * User转换为UserInfoVO
     */
    private UserInfoVO toUserInfoVO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserInfoVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }
} 
package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.RoleConverter;
import top.fblue.watermelon.application.dto.CreateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleResourceDTO;
import top.fblue.watermelon.application.dto.RoleQueryDTO;
import top.fblue.watermelon.application.service.RoleApplicationService;
import top.fblue.watermelon.application.vo.PageVO;
import top.fblue.watermelon.application.vo.RoleVO;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import top.fblue.watermelon.domain.user.entity.User;

/**
 * 角色应用服务实现
 */
@Service
public class RoleApplicationServiceImpl implements RoleApplicationService {

    @Resource
    private RoleDomainService roleDomainService;
    
    @Resource
    private UserDomainService userDomainService;
    
    @Resource
    private RoleConverter roleConverter;

    @Override
    @Transactional
    public RoleVO createRole(CreateRoleDTO createRoleDTO) {
        // 1. 转换DTO为Domain实体
        Role role = roleConverter.toRole(createRoleDTO);
        
        // 2. 通过领域服务创建角色
        Role createdRole = roleDomainService.createRole(role);
        
        // 3. 获取创建后的完整角色信息
        Role roleDetail = roleDomainService.getRoleById(createdRole.getId());
        
        // 4. 组装关联信息并返回
        return buildRoleVO(roleDetail);
    }

    @Override
    public PageVO<RoleVO> getRoleList(RoleQueryDTO queryDTO) {
        // 1. 查询角色列表
        List<Role> roles = roleDomainService.getRoleList(
                queryDTO.getName(),
                queryDTO.getState(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );

        // 2. 统计总数
        Long total = roleDomainService.countRoles(
                queryDTO.getName(),
                queryDTO.getState()
        );

        // 3. 转换为VO
        List<RoleVO> roleVOs = roles.stream()
                .map(this::buildRoleVO)
                .collect(Collectors.toList());

        // 4. 构建分页响应
        return new PageVO<>(
                roleVOs,
                total,
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }

    @Override
    @Transactional
    public boolean updateRole(UpdateRoleDTO updateRoleDTO) {
        // 1. 转换DTO为Domain实体
        Role role = roleConverter.toRole(updateRoleDTO);
        
        // 2. 通过领域服务更新角色
        return roleDomainService.updateRole(role);
    }

    @Override
    @Transactional
    public boolean updateRoleResource(UpdateRoleResourceDTO updateRoleResourceDTO) {
        return roleDomainService.updateRoleResource(
                updateRoleResourceDTO.getId(),
                updateRoleResourceDTO.getResourceIds()
        );
    }

    @Override
    @Transactional
    public boolean deleteRole(Long id) {
        return roleDomainService.deleteRole(id);
    }
    
    /**
     * 构建RoleVO（包含关联信息）
     */
    private RoleVO buildRoleVO(Role role) {
        if (role == null) {
            return null;
        }
        
        // 1. 获取关联的用户信息
        List<Long> userIds = List.of(role.getCreatedBy(), role.getUpdatedBy());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(userIds);
        
        // 2. 转换为VO
        return roleConverter.toVO(role, userMap);
    }
} 
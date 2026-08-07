package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.RoleConverter;
import top.fblue.watermelon.auth.domain.permission.service.PermissionChangeDomainService;
import top.fblue.watermelon.application.dto.CreateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleResourceDTO;
import top.fblue.watermelon.application.dto.RoleQueryDTO;
import top.fblue.watermelon.application.service.RoleApplicationService;
import top.fblue.watermelon.common.response.Page;
import top.fblue.watermelon.application.vo.RoleVO;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Resource
    private ResourceDomainService resourceDomainService;

    /** 权限变更领域服务，用于在当前事务中写入发件箱记录。 */
    @Resource
    private PermissionChangeDomainService permissionChangeDomainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(CreateRoleDTO createRoleDTO) {
        // 1. 转换DTO为Domain实体
        Role role = roleConverter.toRole(createRoleDTO);

        // 2. 通过领域服务创建角色
        Role createdRole = roleDomainService.createRole(role);

        // 3. 组装关联信息并返回
        return roleConverter.toVO(createdRole);
    }

    @Override
    public RoleVO getRoleDetailById(Long id) {
        // 1. 获取角色基本信息
        Role role = roleDomainService.getRoleById(id);

        // 2. 获取关联的用户信息
        List<Long> userIds = List.of(role.getCreatedBy(), role.getUpdatedBy());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(userIds);

        // 3. 获取角色关联的资源ID列表
        List<Long> resourceIds = roleDomainService.getRoleResourceIds(id);

        // 4. 组装关联信息并返回
        return roleConverter.toVO(role, userMap, resourceIds);
    }

    @Override
    public Page<RoleVO> getRoleList(RoleQueryDTO queryDTO) {
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

        // 3. 获取所有关联的用户信息
        Set<Long> userIds = roles.stream()
                .flatMap(role -> Stream.of(role.getCreatedBy(), role.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userDomainService.getUserMapByIds(new ArrayList<>(userIds));

        // 3. 转换为VO
        List<RoleVO> roleVOs = roles.stream()
                .map((role) -> roleConverter.toVO(role, userMap))
                .collect(Collectors.toList());

        // 4. 构建分页响应
        return new Page<>(
                roleVOs,
                total,
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(UpdateRoleDTO updateRoleDTO) {
        // 1. 转换DTO为Domain实体
        Role role = roleConverter.toRole(updateRoleDTO);

        // 2. 查询关联该角色的用户
        List<Long> affectedUserIds = userDomainService.getUserIdsByRoleId(updateRoleDTO.getId());

        // 3. 通过领域服务更新角色
        boolean updated = roleDomainService.updateRole(role);

        // 4. 更新成功后在当前事务中记录关联用户权限变更
        if (updated) {
            permissionChangeDomainService.recordUserPermissionChanges(affectedUserIds);
        }

        // 5. 返回更新结果
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoleResource(UpdateRoleResourceDTO updateRoleResourceDTO) {
        // 1. 校验资源是否存在
        resourceDomainService.validateResourceIds(updateRoleResourceDTO.getResourceIds());

        // 2. 查询关联该角色的用户
        List<Long> affectedUserIds = userDomainService.getUserIdsByRoleId(updateRoleResourceDTO.getId());

        // 3. 更新角色资源关系
        boolean updated = roleDomainService.updateRoleResource(
                updateRoleResourceDTO.getId(),
                updateRoleResourceDTO.getResourceIds()
        );

        // 4. 更新成功后在当前事务中记录关联用户权限变更
        if (updated) {
            permissionChangeDomainService.recordUserPermissionChanges(affectedUserIds);
        }

        // 5. 返回更新结果
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        // 1. 删除角色前查询关联用户，避免关系删除后无法确定影响范围
        List<Long> affectedUserIds = userDomainService.getUserIdsByRoleId(id);

        // 2. 通过领域服务删除角色
        boolean deleted = roleDomainService.deleteRole(id);

        // 3. 删除成功后在当前事务中记录关联用户权限变更
        if (deleted) {
            permissionChangeDomainService.recordUserPermissionChanges(affectedUserIds);
        }

        // 4. 返回删除结果
        return deleted;
    }
}

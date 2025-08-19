package top.fblue.watermelon.domain.role.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.domain.role.repository.RoleRepository;
import top.fblue.watermelon.domain.role.repository.RoleResourceRepository;
import top.fblue.watermelon.domain.role.service.RoleDomainService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色领域服务实现
 */
@Service
public class RoleDomainServiceImpl implements RoleDomainService {

    @Resource
    private RoleRepository roleRepository;
    
    @Resource
    private RoleResourceRepository roleResourceRepository;

    @Override
    public Role createRole(Role role) {
        // 1. 检查角色名称是否已存在
        if (existsByName(role.getName())) {
            throw new BusinessException("角色名称已存在");
        }
        
        // 2. 保存角色
        return roleRepository.save(role);
    }

    @Override
    public Role getRoleById(Long id) {
        if (id == null) {
            throw new BusinessException("角色ID不能为空");
        }
        
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        return role;
    }

    @Override
    public List<Role> getRoleByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        return roleRepository.findByIds(ids);
    }

    @Override
    public void validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        List<Role> roles = getRoleByIds(roleIds);
        if (roles.size() == roleIds.size()) {
            return;
        }
        Set<Long> existRoleIdSet = roles.stream().map(Role::getId).collect(Collectors.toSet());
        List<Long> notExistRoleIds = roleIds.stream()
                .filter(id -> !existRoleIdSet.contains(id))
                .toList();
        if (!notExistRoleIds.isEmpty()) {
            String ids = notExistRoleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            throw new BusinessException("以下角色ID不存在: " + ids);
        }
    }

    @Override
    public List<Role> getRoleList(String name, Integer state, int pageNum, int pageSize) {
        return roleRepository.findByCondition(name, state, pageNum, pageSize);
    }

    @Override
    public Long countRoles(String name, Integer state) {
        return roleRepository.countByCondition(name, state);
    }

    @Override
    public boolean updateRole(Role role) {
        // 1. 检查角色是否存在
        Role existingRole = getRoleById(role.getId());
        
        // 2. 如果修改了名称，检查新名称是否已存在
        if (!existingRole.getName().equals(role.getName())) {
            if (existsByName(role.getName())) {
                throw new BusinessException("角色名称已存在");
            }
        }
        
        // 3. 更新角色
        return roleRepository.update(role);
    }

    @Override
    public boolean updateRoleResource(Long roleId, List<Long> resourceIds) {
        // 1. 检查角色是否存在
        getRoleById(roleId);

        // 2. 查询现有的角色资源关系
        List<Long> existingResourceIds = roleResourceRepository.findResourceIdsByRoleId(roleId);

        // 3. 计算需要删除和新增的资源ID
        Set<Long> resourceIdSet = new HashSet<>(resourceIds);
        List<Long> toDelete = existingResourceIds.stream()
                .filter(id -> !resourceIdSet.contains(id))
                .collect(Collectors.toList());
        
        List<Long> toInsert = resourceIds.stream()
                .filter(id -> !existingResourceIds.contains(id))
                .collect(Collectors.toList());

        // 4. 批量删除不需要的关系
        if (!toDelete.isEmpty()) {
            roleResourceRepository.deleteBatch(roleId, toDelete);
        }

        // 5. 批量新增新的关系
        if (!toInsert.isEmpty()) {
            roleResourceRepository.insertBatch(roleId, toInsert);
        }

        return true;
    }
    
    @Override
    public List<Long> getRoleResourceIds(Long roleId) {
        return roleResourceRepository.findResourceIdsByRoleId(roleId);
    }

    @Override
    public List<Long> getRoleResourceIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return roleResourceRepository.findResourceIdsByRoleIds(roleIds);
    }

    @Override
    public boolean deleteRole(Long id) {
        // 1. 检查角色是否存在
        getRoleById(id);
        
        // 2. 删除角色资源关系
        roleResourceRepository.deleteByRoleId(id);
        
        // 3. 删除角色
        return roleRepository.delete(id);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
} 
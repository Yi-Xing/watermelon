package top.fblue.watermelon.domain.role.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.domain.role.repository.RoleRepository;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 角色领域服务实现
 */
@Service
public class RoleDomainServiceImpl implements RoleDomainService {

    @Resource
    private RoleRepository roleRepository;
    
    @Resource
    private ResourceDomainService resourceDomainService;

    @Override
    public Role createRole(Role role) {
        // 1. 检查角色名称是否已存在
        if (existsByName(role.getName())) {
            throw new IllegalArgumentException("角色名称已存在");
        }
        
        // 2. 保存角色
        return roleRepository.save(role);
    }

    @Override
    public Role getRoleById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        
        return role;
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
                throw new IllegalArgumentException("角色名称已存在");
            }
        }
        
        // 3. 更新角色
        return roleRepository.update(role);
    }

    @Override
    public boolean updateRoleResource(Long roleId, List<Long> resourceIds) {
        // 1. 检查角色是否存在
        getRoleById(roleId);
        
        // 2. 检查资源是否都存在
        if (resourceIds != null && !resourceIds.isEmpty()) {
            Set<Long> invalidResourceIds = new HashSet<>();
            for (Long resourceId : resourceIds) {
                if (!resourceDomainService.existsById(resourceId)) {
                    invalidResourceIds.add(resourceId);
                }
            }
            
            if (!invalidResourceIds.isEmpty()) {
                throw new IllegalArgumentException("以下资源ID不存在：" + invalidResourceIds);
            }
        }
        
        // 3. 更新角色资源关系
        return roleRepository.updateRoleResource(roleId, resourceIds);
    }

    @Override
    public boolean deleteRole(Long id) {
        // 1. 检查角色是否存在
        getRoleById(id);
        
        // 2. 删除角色
        return roleRepository.delete(id);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
} 
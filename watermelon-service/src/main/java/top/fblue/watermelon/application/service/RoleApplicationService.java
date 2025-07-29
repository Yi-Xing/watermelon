package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.CreateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleResourceDTO;
import top.fblue.watermelon.application.dto.RoleQueryDTO;
import top.fblue.watermelon.common.response.Page;
import top.fblue.watermelon.application.vo.RoleVO;

/**
 * 角色应用服务接口
 */
public interface RoleApplicationService {
    
    /**
     * 创建角色
     */
    RoleVO createRole(CreateRoleDTO createRoleDTO);
    
    /**
     * 根据ID获取角色详情（包含关联资源）
     */
    RoleVO getRoleDetailById(Long id);
    
    /**
     * 分页查询角色列表
     */
    Page<RoleVO> getRoleList(RoleQueryDTO queryDTO);
    
    /**
     * 更新角色
     */
    boolean updateRole(UpdateRoleDTO updateRoleDTO);
    
    /**
     * 更新角色资源
     */
    boolean updateRoleResource(UpdateRoleResourceDTO updateRoleResourceDTO);
    
    /**
     * 删除角色
     */
    boolean deleteRole(Long id);
} 
package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.service.RoleApplicationService;
import top.fblue.watermelon.application.dto.CreateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleDTO;
import top.fblue.watermelon.application.dto.UpdateRoleResourceDTO;
import top.fblue.watermelon.application.dto.RoleQueryDTO;
import top.fblue.watermelon.application.vo.PageVO;
import top.fblue.watermelon.application.vo.RoleVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/role")
@Validated
public class RoleController {
    
    @Resource
    private RoleApplicationService roleApplicationService;
    
    /**
     * 创建角色
     */
    @PostMapping
    public ApiResponse<RoleVO> createRole(@Valid @RequestBody CreateRoleDTO createRoleDTO) {
        RoleVO role = roleApplicationService.createRole(createRoleDTO);
        return ApiResponse.success(role, "角色创建成功");
    }
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    public ApiResponse<PageVO<RoleVO>> getRoleList(@Valid RoleQueryDTO queryDTO) {
        PageVO<RoleVO> pageVO = roleApplicationService.getRoleList(queryDTO);
        return ApiResponse.success(pageVO, "获取角色列表成功");
    }
    
    /**
     * 更新角色
     */
    @PutMapping
    public ApiResponse<Boolean> updateRole(@Valid @RequestBody UpdateRoleDTO updateRoleDTO) {
        boolean result = roleApplicationService.updateRole(updateRoleDTO);
        if (result) {
            return ApiResponse.success(true, "角色更新成功");
        } else {
            return ApiResponse.error("角色更新失败", false);
        }
    }
    
    /**
     * 更新角色资源
     */
    @PutMapping("/resource")
    public ApiResponse<Boolean> updateRoleResource(@Valid @RequestBody UpdateRoleResourceDTO updateRoleResourceDTO) {
        boolean result = roleApplicationService.updateRoleResource(updateRoleResourceDTO);
        if (result) {
            return ApiResponse.success(true, "角色资源更新成功");
        } else {
            return ApiResponse.error("角色资源更新失败", false);
        }
    }
    
    /**
     * 根据ID获取角色详情（包含关联资源）
     */
    @GetMapping("/{id}")
    public ApiResponse<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO role = roleApplicationService.getRoleDetailById(id);
        return ApiResponse.success(role, "获取角色详情成功");
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteRole(@PathVariable Long id) {
        boolean result = roleApplicationService.deleteRole(id);
        if (result) {
            return ApiResponse.success(true, "角色删除成功");
        } else {
            return ApiResponse.error("角色删除失败", false);
        }
    }
} 
package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.service.UserApplicationService;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UpdateUserDTO;
import top.fblue.watermelon.application.dto.ResetPasswordDTO;
import top.fblue.watermelon.application.dto.UserQueryDTO;
import top.fblue.watermelon.common.response.Page;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    
    @Resource
    private UserApplicationService userApplicationService;
    
    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<UserVO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserVO user = userApplicationService.createUser(createUserDTO);
        return ApiResponse.success(user, "用户创建成功");
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public ApiResponse<Page<UserVO>> getUserList(@Valid UserQueryDTO queryDTO) {
        Page<UserVO> page = userApplicationService.getUserList(queryDTO);
        return ApiResponse.success(page, "获取用户列表成功");
    }
    
    /**
     * 根据ID获取用户详情（包含关联角色）
     */
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = userApplicationService.getUserDetailById(id);
        return ApiResponse.success(user, "获取用户详情成功");
    }

    /**
     * 更新用户
     */
    @PutMapping
    public ApiResponse<Boolean> updateUser(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
        boolean result = userApplicationService.updateUser(updateUserDTO);
        if (result) {
            return ApiResponse.success(true, "用户更新成功");
        } else {
            return ApiResponse.error("用户更新失败", false);
        }
    }

    /**
     * 重设密码
     */
    @PutMapping("/password")
    public ApiResponse<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        boolean result = userApplicationService.resetPassword(resetPasswordDTO);
        if (result) {
            return ApiResponse.success(true, "密码重设成功");
        } else {
            return ApiResponse.error("密码重设失败", false);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userApplicationService.deleteUser(id);
        if (result) {
            return ApiResponse.success(true, "用户删除成功");
        } else {
            return ApiResponse.error("用户删除失败", false);
        }
    }
}

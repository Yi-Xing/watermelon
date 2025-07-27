package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.service.UserApplicationService;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UserQueryDTO;
import top.fblue.watermelon.application.vo.PageVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;

/**
 * 用户控制器
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
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = userApplicationService.getUserById(id);
        return ApiResponse.success(user, "获取用户信息成功");
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
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public ApiResponse<PageVO<UserVO>> getUserList(@Valid UserQueryDTO queryDTO) {
        PageVO<UserVO> pageVO = userApplicationService.getUserList(queryDTO);
        return ApiResponse.success(pageVO, "获取用户列表成功");
    }
}

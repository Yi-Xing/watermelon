package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.service.UserApplicationService;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<ApiResponse<UserVO>> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserVO user = userApplicationService.createUser(createUserDTO);
        return ResponseEntity.ok(ApiResponse.success(user, "用户创建成功"));
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserVO>> getUserById(@PathVariable Long id) {
        UserVO user = userApplicationService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "获取用户信息成功"));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@PathVariable Long id) {
        boolean result = userApplicationService.deleteUser(id);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(true, "用户删除成功"));
        } else {
            return ResponseEntity.ok(ApiResponse.error(400, "用户删除失败", false));
        }
    }
}

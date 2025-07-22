package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.UserApplicationService;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;

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
    public ResponseEntity<UserVO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserVO user = userApplicationService.createUser(createUserDTO);
        return ResponseEntity.ok(user);
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = userApplicationService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userApplicationService.deleteUser(id);
        return ResponseEntity.ok(result);
    }
}

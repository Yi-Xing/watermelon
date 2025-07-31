package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.LoginApplicationService;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class LoginController {
    
    @Resource
    private  LoginApplicationService loginApplicationService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = loginApplicationService.login(loginDTO);
        return ApiResponse.success(loginVO, "登录成功");
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        boolean success = loginApplicationService.logout(token);
        if (success) {
            return ApiResponse.success(null, "退出登录成功");
        } else {
            return ApiResponse.error(400, "退出登录失败");
        }
    }
}

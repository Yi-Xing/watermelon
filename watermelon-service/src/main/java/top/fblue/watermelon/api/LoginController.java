package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.LoginApplicationService;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;

/**
 * 登录相关
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class LoginController {

    @Resource
    private LoginApplicationService loginApplicationService;

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
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization") String authHeader) {
        loginApplicationService.logout(authHeader);
        return ApiResponse.success(null, "退出登录成功");
    }

    /**
     * 刷新token
     */
    @PostMapping("/token/refresh")
    public ApiResponse<String> refreshToken(@RequestHeader(value = "Authorization") String authHeader) {
        String newToken = loginApplicationService.refreshToken(authHeader);
        return ApiResponse.success(newToken, "Token刷新成功");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public ApiResponse<CurrentUserVO> getCurrentUser() {
        CurrentUserVO userVO = loginApplicationService.getCurrentUser();
        return ApiResponse.success(userVO, "获取当前用户信息成功");
    }
}

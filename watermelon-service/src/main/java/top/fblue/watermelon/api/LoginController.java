package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.dto.LoginDTO;
import top.fblue.watermelon.application.service.UserAuthApplicationService;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.Valid;
import top.fblue.auth.annotation.SsoPublic;

/**
 * 登录相关
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class LoginController {

    /** 用户登录及身份查询应用服务。 */
    @Resource
    private UserAuthApplicationService userAuthApplicationService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @SsoPublic
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userAuthApplicationService.login(loginDTO);
        return ApiResponse.success(loginVO, "登录成功");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization") String authHeader) {
        userAuthApplicationService.logout(authHeader);
        return ApiResponse.success(null, "退出登录成功");
    }

    /**
     * 刷新token
     */
    @PostMapping("/token/refresh")
    public ApiResponse<String> refreshToken(@RequestHeader(value = "Authorization") String authHeader) {
        String newToken = userAuthApplicationService.refreshToken(authHeader);
        return ApiResponse.success(newToken, "Token刷新成功");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public ApiResponse<CurrentUserVO> getCurrentUser() {
        CurrentUserVO userVO = userAuthApplicationService.getCurrentUser();
        return ApiResponse.success(userVO, "获取当前用户信息成功");
    }
}

package top.fblue.watermelon.auth.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.auth.application.service.UserAuthApplicationService;
import top.fblue.watermelon.auth.common.context.UserContext;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;

import java.util.Arrays;

import static top.fblue.watermelon.auth.common.constant.UserConst.AUTH_STATE_COOKIE;

/**
 * 对外 HTTP 接口：生成回调地址
 * <p>
 * 调用前置条件：请求已通过 TokenAuthInterceptor（header 中携带有效 token）
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthCallbackController {

    private final UserAuthApplicationService userAuthApplicationService;

    /**
     * 生成回调地址
     *
     * @param request     入参：return_url, state
     * @param httpRequest HTTP 请求（用于读取 cookie）
     * @return 拼接了 code 和 state 的回调地址
     */
    @PostMapping("/callback")
    public ApiResponse<CallbackResponse> generateCallback(
            @RequestBody @Valid CallbackRequest request,
            HttpServletRequest httpRequest) {

        // 从 request 中获取当前登录用户（由 TokenAuthInterceptor 注入）
        UserDTO currentUser = UserContext.getCurrentUserInfo();

        // 从 cookie 中读取 state
        String cookieState = extractCookieState(httpRequest);

        CallbackResponse response = userAuthApplicationService.generateCallback(
                currentUser.getUserId(),
                currentUser.getJti(),
                currentUser.getExpiresIn(),
                request.getReturnUrl(),
                request.getState(),
                cookieState
        );
        return ApiResponse.success(response);
    }

    private String extractCookieState(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> AUTH_STATE_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}

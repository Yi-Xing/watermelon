package top.fblue.watermelon.auth.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.fblue.auth.context.SsoHttpContext;
import top.fblue.common.response.ApiResponse;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;
import top.fblue.watermelon.auth.application.service.SsoAuthorizationApplicationService;

/**
 * 用户中心 SSO 授权接口。
 */
@RestController
@RequestMapping("/api/sso")
@RequiredArgsConstructor
public class SsoAuthorizationController {

    /** SSO 授权应用服务。 */
    private final SsoAuthorizationApplicationService authorizationApplicationService;

    /**
     * 校验当前登录用户及客户端授权请求，并生成客户端回调地址。
     *
     * @param request 客户端授权请求
     * @return 包含一次性授权码的回调信息
     */
    @PostMapping("/authorize")
    public ApiResponse<CallbackResponse> authorize(@RequestBody @Valid CallbackRequest request) {
        return ApiResponse.success(authorizationApplicationService.generateCallback(
                SsoHttpContext.getCurrentUserInfo(), request));
    }
}

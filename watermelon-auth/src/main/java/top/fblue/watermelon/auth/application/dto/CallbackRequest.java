package top.fblue.watermelon.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成回调地址请求
 */
@Data
public class CallbackRequest {

    /**
     * 回调地址（系统A/B 的前端页面地址），需在白名单域名内
     */
    @NotBlank(message = "return_url 不能为空")
    private String returnUrl;

    /**
     * 登录时生成的随机数（需与 cookie 中的 auth_state 一致）
     */
    @NotBlank(message = "state 不能为空")
    private String state;
}

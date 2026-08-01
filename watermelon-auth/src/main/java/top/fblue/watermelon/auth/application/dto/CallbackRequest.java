package top.fblue.watermelon.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成回调地址请求
 */
@Data
public class CallbackRequest {

    /** 发起授权请求的 SSO 客户端标识。 */
    @NotBlank(message = "clientId 不能为空")
    private String clientId;

    /** 授权完成后的客户端回调地址。 */
    @NotBlank(message = "redirectUri 不能为空")
    private String redirectUri;

    /**
     * 登录时生成的随机数（需与 cookie 中的 auth_state 一致）
     */
    @NotBlank(message = "state 不能为空")
    private String state;
}

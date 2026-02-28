package top.fblue.watermelon.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成回调地址响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackResponse {

    /**
     * 拼接了 code、state 参数的回调地址
     * 格式：{returnUrl}?code={code}&state={state}
     * 前端拿到后，通知用户中心将用户重定向至此地址，
     * 或直接使用 code 换取用户信息
     */
    private String callbackUrl;
}

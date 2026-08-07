package top.fblue.watermelon.auth.application.converter;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;

/**
 * 用户中心 SSO 授权结果转换器。
 */
@Component
public class SsoAuthorizationConverter {

    /**
     * 将授权码和 state 转换为客户端回调响应。
     *
     * @param redirectUri 已校验的客户端回调地址
     * @param code 一次性授权码
     * @param state 客户端防伪状态值
     * @return 客户端回调响应
     */
    public CallbackResponse toCallbackResponse(String redirectUri, String code, String state) {
        String callbackUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
        return CallbackResponse.builder().callbackUrl(callbackUrl).build();
    }

    /**
     * 将用户和授权码信息转换为授权码兑换响应。
     *
     * @param user SSO 用户
     * @param codeInfo 已消费的授权码信息
     * @return 授权码兑换响应
     */
    public CodeExchangeResponse toCodeExchangeResponse(User user, AuthCodeInfo codeInfo) {
        return CodeExchangeResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .sid(codeInfo.getSid())
                .sessionExpireAt(codeInfo.getSessionExpireAt())
                .build();
    }
}

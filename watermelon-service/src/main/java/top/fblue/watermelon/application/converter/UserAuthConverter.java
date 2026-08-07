package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.application.vo.LoginVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.dto.UserTokenDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 用户登录和 SSO 会话对象转换器。
 */
@Component
public class UserAuthConverter {

    /**
     * 构造用户登录响应。
     *
     * @param userInfo 用户信息
     * @param token 访问令牌
     * @return 登录响应
     */
    public LoginVO toLoginVO(UserVO userInfo, String token) {
        return LoginVO.builder()
                .userInfo(userInfo)
                .token(token)
                .build();
    }

    /**
     * 将 SSO 身份转换为兼容现有业务接口的用户令牌 DTO。
     *
     * @param principal SSO 用户身份
     * @param token 原始访问令牌；无需回传时可为 {@code null}
     * @return 用户令牌 DTO
     */
    public UserTokenDTO toUserTokenDTO(SsoPrincipal principal, String token) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime createdTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(principal.getIssuedAtEpochSeconds()), zoneId);
        LocalDateTime expireTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(principal.getExpiresAtEpochSeconds()), zoneId);
        return UserTokenDTO.builder()
                .userId(principal.getUserId())
                .sid(principal.getSid())
                .jti(principal.getJti())
                .token(token)
                .createdTime(createdTime)
                .expireTime(expireTime)
                .build();
    }

    /**
     * 将当前登录身份转换为全局会话注销请求。
     *
     * @param principal 当前 SSO 用户身份
     * @param clientId 用户中心自身的客户端标识
     * @param reason 注销原因
     * @return 全局会话注销请求
     */
    public LogoutRpcRequest toLogoutRequest(SsoPrincipal principal, String clientId, String reason) {
        return LogoutRpcRequest.builder()
                .sid(principal.getSid())
                .clientId(clientId)
                .jti(principal.getJti())
                .reason(reason)
                .build();
    }
}

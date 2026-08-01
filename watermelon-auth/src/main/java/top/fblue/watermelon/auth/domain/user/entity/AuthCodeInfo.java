package top.fblue.watermelon.auth.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 授权码存储信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeInfo {

    /**
     * 授权码所属用户 ID。
     */
    private Long userId;

    /**
     * 授权码关联的全局 SSO 会话标识。
     */
    private String sid;

    /**
     * 申请授权码的 SSO 客户端标识。
     */
    private String clientId;

    /**
     * 申请授权码时已校验并规范化的客户端回调地址。
     */
    private String redirectUri;

    /**
     * 关联全局会话的过期时间，Unix 秒级时间戳。
     */
    private long sessionExpireAt;
}

package top.fblue.watermelon.auth.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户中心维护的全局 SSO 会话信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoSessionInfo {

    /** 全局 SSO 会话唯一标识。 */
    private String sid;
    /** 会话所属用户 ID。 */
    private Long userId;
    /** 创建会话时记录的登录设备标识。 */
    private String deviceCode;
    /** 会话过期时间，Unix 秒级时间戳。 */
    private long expireAtEpochSeconds;
}

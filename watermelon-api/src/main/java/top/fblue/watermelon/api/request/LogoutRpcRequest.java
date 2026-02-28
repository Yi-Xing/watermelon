package top.fblue.watermelon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 退出登录 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRpcRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * JWT 唯一标识
     */
    private String jti;

    /**
     * token 剩余有效时间（秒），用于设置黑名单 TTL
     */
    private long expiresIn;

    /**
     * 设备 code（作为黑名单 value 存储）
     */
    private String deviceCode;
}

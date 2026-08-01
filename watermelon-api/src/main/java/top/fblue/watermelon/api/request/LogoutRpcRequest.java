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

    /** Java 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 全局登录会话 ID。 */
    private String sid;

    /** 发起退出的 Client。 */
    private String clientId;

    /** 发起退出的本地 Token ID，仅用于审计。 */
    private String jti;

    /** 退出原因。 */
    private String reason;
}

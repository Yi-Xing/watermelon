package top.fblue.watermelon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 其他系统吊销全局 sid 的 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRevokeRequest implements Serializable {

    /** Java 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 退出事件幂等 ID。 */
    private String eventId;

    /** 需要撤销的全局登录会话。 */
    private String sid;

    /** 全局会话绝对过期时间，epoch seconds。 */
    private long sessionExpireAt;

    /** 退出原因。 */
    private String reason;
}

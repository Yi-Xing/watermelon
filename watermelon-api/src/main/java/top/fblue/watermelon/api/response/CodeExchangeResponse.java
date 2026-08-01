package top.fblue.watermelon.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * code 换用户信息 RPC 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExchangeResponse implements Serializable {

    /** Java 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String username;

    /** 全局登录会话 ID。 */
    private String sid;

    /** 全局会话绝对过期时间，epoch seconds。 */
    private long sessionExpireAt;
}

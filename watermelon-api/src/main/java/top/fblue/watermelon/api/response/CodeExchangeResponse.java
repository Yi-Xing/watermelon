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

    /**
     * JWT 唯一标识（jti）
     */
    private String jti;

    /**
     * token 剩余有效时间（秒）
     */
    private long expiresIn;
}

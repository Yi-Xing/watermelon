package top.fblue.watermelon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * code 换用户信息 RPC 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExchangeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权码
     */
    private String code;

    /**
     * 调用方系统标识（用于记录 jti -> 系统 的映射，便于退出登录时通知）
     * 例如：system-a
     */
    private String callerSystem;
}

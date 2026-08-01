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

    /** Java 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权码
     */
    private String code;

    /** 调用方 SSO Client 标识，例如 banana。 */
    private String clientId;

    /** 必须与签发 code 时绑定的回调地址完全一致。 */
    private String redirectUri;
}

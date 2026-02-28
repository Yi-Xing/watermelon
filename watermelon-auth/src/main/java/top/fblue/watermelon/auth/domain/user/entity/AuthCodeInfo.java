package top.fblue.watermelon.auth.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 授权码存储信息（code -> userId、jti、expiresIn 的映射）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * JWT 唯一标识
     */
    private String jti;

    /**
     * token 剩余有效时间（秒）
     */
    private long expiresIn;
}

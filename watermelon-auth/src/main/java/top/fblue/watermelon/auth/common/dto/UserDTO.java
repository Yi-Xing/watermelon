package top.fblue.watermelon.auth.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


/**
 * 用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * JWT 唯一标识（jti claim），用于黑名单校验
     */
    private String jti;

    /**
     * token 剩余有效时间（秒），由 TokenAuthInterceptor 从 JWT claims 解析填充
     */
    private long expiresIn;

    /**
     * 用户关联的角色ID
     */
    Set<Long> roles;

    /**
     * 用户权限版本
     */
    long permVersion;
}
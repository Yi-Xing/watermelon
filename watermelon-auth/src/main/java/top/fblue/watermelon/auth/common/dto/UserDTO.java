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
     * 用户关联的角色ID
     */
    Set<Long> roles;
    /**
     *  用户权限版本
     */
    long permVersion;
}
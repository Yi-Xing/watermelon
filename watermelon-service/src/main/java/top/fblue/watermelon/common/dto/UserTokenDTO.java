package top.fblue.watermelon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * Token值
     */
    private String token;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
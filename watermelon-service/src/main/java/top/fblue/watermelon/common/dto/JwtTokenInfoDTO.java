package top.fblue.watermelon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JWT令牌信息DTO
 * 用于返回JWT令牌的详细信息
 * 作为通用DTO，可在各层之间使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenInfoDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * JWT令牌
     */
    private String token;
    
    /**
     * 令牌创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 令牌过期时间
     */
    private LocalDateTime expiresAt;
}

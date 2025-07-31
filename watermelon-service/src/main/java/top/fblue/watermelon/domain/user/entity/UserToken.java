package top.fblue.watermelon.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户Token实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserToken {
    
    
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
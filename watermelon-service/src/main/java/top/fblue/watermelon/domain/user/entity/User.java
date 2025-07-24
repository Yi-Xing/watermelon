package top.fblue.watermelon.domain.user.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户聚合根
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private Integer state;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
    
    // 关联的用户信息（可选，用于展示时的详细信息）
    private UserBasicInfo createdByUser;
    private UserBasicInfo updatedByUser;

}

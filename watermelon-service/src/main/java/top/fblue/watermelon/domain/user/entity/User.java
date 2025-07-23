package top.fblue.watermelon.domain.user.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户聚合根
 */
@Data
@Builder
public class User {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private Integer state;  // 状态：1 启用 2 禁用
    private String remark;
}

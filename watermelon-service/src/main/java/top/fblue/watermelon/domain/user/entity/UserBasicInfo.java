package top.fblue.watermelon.domain.user.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户基本信息 - 贫血模型
 * 仅包含数据字段，不包含业务逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfo {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
}

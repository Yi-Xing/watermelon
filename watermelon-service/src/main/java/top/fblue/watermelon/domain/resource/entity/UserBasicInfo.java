package top.fblue.watermelon.domain.resource.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

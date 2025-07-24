package top.fblue.watermelon.domain.user.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 包含关联用户信息的用户Domain对象
 * Domain层的聚合对象，包含完整的用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithRelatedInfo {
    
    /**
     * 主用户信息
     */
    private User user;
    
    /**
     * 创建人信息
     */
    private UserBasicInfo createdByUser;
    
    /**
     * 更新人信息
     */
    private UserBasicInfo updatedByUser;
}
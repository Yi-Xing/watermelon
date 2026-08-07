package top.fblue.watermelon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户中心发送给业务系统的权限缓存失效通知。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionChangeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 幂等事件 ID。 */
    private String eventId;

    /** 目标业务系统编码。 */
    private String systemCode;

    /** 受影响用户；为空表示系统级权限策略发生变化。 */
    private Long userId;

    /** 用户维度的权限版本。 */
    private long userPermissionVersion;

    /** 系统维度的权限版本。 */
    private long systemPermissionVersion;
}

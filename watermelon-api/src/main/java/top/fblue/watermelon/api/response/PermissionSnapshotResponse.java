package top.fblue.watermelon.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户在单个业务系统中的有效权限快照。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSnapshotResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户中心用户 ID。 */
    private Long userId;

    /** 权限所属系统。 */
    private String systemCode;

    /** 当前用户维度的权限版本。 */
    private long userPermissionVersion;

    /** 当前系统维度的权限版本。 */
    private long systemPermissionVersion;

    /** 页面资源编码，保留系统前缀。 */
    private List<String> pageCodes;

    /** 按钮资源编码，保留系统前缀。 */
    private List<String> buttonCodes;

    /** 接口资源编码，仅供业务系统后端鉴权。 */
    private List<String> apiCodes;
}

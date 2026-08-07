package top.fblue.watermelon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询用户权限快照的 RPC 请求。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSnapshotRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户中心用户 ID。 */
    private Long userId;

    /** 调用方系统编码，必须与 Dubbo 调用方应用名一致。 */
    private String systemCode;
}

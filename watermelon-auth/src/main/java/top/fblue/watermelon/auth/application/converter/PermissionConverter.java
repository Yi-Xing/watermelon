package top.fblue.watermelon.auth.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.api.response.PermissionSnapshotResponse;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;

/**
 * 权限快照转换器。
 */
@Component
public class PermissionConverter {

    /**
     * 将权限资源和版本信息转换为对外权限快照。
     *
     * @param snapshot 权限领域快照
     * @return 对外权限快照
     */
    public PermissionSnapshotResponse toResponse(PermissionSnapshot snapshot) {
        return PermissionSnapshotResponse.builder()
                .userId(snapshot.userId())
                .systemCode(snapshot.systemCode())
                .userPermissionVersion(snapshot.userPermissionVersion())
                .systemPermissionVersion(snapshot.systemPermissionVersion())
                .pageCodes(snapshot.codesOfType(PermissionResourceTypeEnum.PAGE))
                .buttonCodes(snapshot.codesOfType(PermissionResourceTypeEnum.BUTTON))
                .apiCodes(snapshot.codesOfType(PermissionResourceTypeEnum.API))
                .build();
    }
}

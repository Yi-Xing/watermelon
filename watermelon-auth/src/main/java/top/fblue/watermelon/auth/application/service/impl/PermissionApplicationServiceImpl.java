package top.fblue.watermelon.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.api.request.PermissionSnapshotRequest;
import top.fblue.watermelon.api.response.PermissionSnapshotResponse;
import top.fblue.watermelon.auth.application.converter.PermissionConverter;
import top.fblue.watermelon.auth.application.service.PermissionApplicationService;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.auth.domain.permission.service.PermissionQueryDomainService;

/**
 * 计算用户在单个业务系统中的有效权限快照。
 */
@Service
@RequiredArgsConstructor
public class PermissionApplicationServiceImpl implements PermissionApplicationService {

    /** 权限查询领域服务。 */
    private final PermissionQueryDomainService permissionQueryDomainService;

    /** 权限快照转换器。 */
    private final PermissionConverter permissionConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissionSnapshotResponse getPermissionSnapshot(PermissionSnapshotRequest request) {
        // 1. 通过领域服务查询用户在目标业务系统中的权限快照
        PermissionSnapshot snapshot = permissionQueryDomainService.getPermissionSnapshot(
                request.getUserId(), request.getSystemCode());

        // 2. 转换为对外权限快照并返回
        return permissionConverter.toResponse(snapshot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasApiPermission(Long userId, String systemCode, String resourceCode) {
        // 1. 委托权限查询领域服务完成接口权限判断
        return permissionQueryDomainService.hasApiPermission(userId, systemCode, resourceCode);
    }
}

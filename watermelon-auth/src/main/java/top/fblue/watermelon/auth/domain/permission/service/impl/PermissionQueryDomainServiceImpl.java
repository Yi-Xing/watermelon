package top.fblue.watermelon.auth.domain.permission.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionResourceQueryRepository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionVersionRepository;
import top.fblue.watermelon.auth.domain.permission.service.PermissionQueryDomainService;

import java.util.List;

/**
 * 权限查询领域服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionQueryDomainServiceImpl implements PermissionQueryDomainService {

    /** 有效权限资源查询仓储。 */
    private final PermissionResourceQueryRepository permissionResourceQueryRepository;

    /** 权限版本仓储。 */
    private final PermissionVersionRepository permissionVersionRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissionSnapshot getPermissionSnapshot(Long userId, String systemCode) {
        // 1. 查询用户在目标业务系统中的有效权限资源
        List<PermissionResource> resources = permissionResourceQueryRepository.findEffectiveResources(
                userId, systemCode);

        // 2. 查询用户级和系统级权限版本
        long userPermissionVersion = permissionVersionRepository.getUserPermissionVersion(userId);
        long systemPermissionVersion = permissionVersionRepository.getSystemPermissionVersion(systemCode);

        // 3. 构建不可变权限领域快照并返回
        return new PermissionSnapshot(
                userId, systemCode, resources, userPermissionVersion, systemPermissionVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasApiPermission(Long userId, String systemCode, String resourceCode) {
        // 1. 查询用户在目标系统中的有效权限资源
        List<PermissionResource> resources = permissionResourceQueryRepository.findEffectiveResources(
                userId, systemCode);

        // 2. 只匹配接口类型及完整资源编码，接口鉴权不额外读取版本
        return resources.stream()
                .anyMatch(resource -> PermissionResourceTypeEnum.API == resource.type()
                        && resourceCode.equals(resource.code()));
    }
}

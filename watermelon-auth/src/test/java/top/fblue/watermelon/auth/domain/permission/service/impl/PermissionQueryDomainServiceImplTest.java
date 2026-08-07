package top.fblue.watermelon.auth.domain.permission.service.impl;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionResourceQueryRepository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionVersionRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionQueryDomainServiceImplTest {

    @Test
    void shouldBuildPermissionSnapshotThroughRepositories() {
        PermissionResourceQueryRepository resourceRepository = mock(PermissionResourceQueryRepository.class);
        PermissionVersionRepository versionRepository = mock(PermissionVersionRepository.class);
        PermissionQueryDomainServiceImpl domainService =
                new PermissionQueryDomainServiceImpl(resourceRepository, versionRepository);
        List<PermissionResource> resources = List.of(
                new PermissionResource("banana:home.page", PermissionResourceTypeEnum.PAGE));
        when(resourceRepository.findEffectiveResources(7L, "banana")).thenReturn(resources);
        when(versionRepository.getUserPermissionVersion(7L)).thenReturn(3L);
        when(versionRepository.getSystemPermissionVersion("banana")).thenReturn(5L);

        PermissionSnapshot snapshot = domainService.getPermissionSnapshot(7L, "banana");

        assertEquals(resources, snapshot.resources());
        assertEquals(3L, snapshot.userPermissionVersion());
        assertEquals(5L, snapshot.systemPermissionVersion());
    }

    @Test
    void shouldOnlyMatchApiResource() {
        PermissionResourceQueryRepository resourceRepository = mock(PermissionResourceQueryRepository.class);
        PermissionVersionRepository versionRepository = mock(PermissionVersionRepository.class);
        PermissionQueryDomainServiceImpl domainService =
                new PermissionQueryDomainServiceImpl(resourceRepository, versionRepository);
        when(resourceRepository.findEffectiveResources(7L, "watermelon")).thenReturn(List.of(
                new PermissionResource("watermelon:admin.users.page", PermissionResourceTypeEnum.PAGE),
                new PermissionResource("watermelon:GET:/api/admin/users", PermissionResourceTypeEnum.API)
        ));

        assertTrue(domainService.hasApiPermission(
                7L, "watermelon", "watermelon:GET:/api/admin/users"));
        assertFalse(domainService.hasApiPermission(
                7L, "watermelon", "watermelon:admin.users.page"));
    }
}

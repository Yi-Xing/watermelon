package top.fblue.watermelon.auth.application.service.impl;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.api.request.PermissionSnapshotRequest;
import top.fblue.watermelon.api.response.PermissionSnapshotResponse;
import top.fblue.watermelon.auth.application.converter.PermissionConverter;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.auth.domain.permission.service.PermissionQueryDomainService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionApplicationServiceImplTest {

    @Test
    void shouldConvertDomainSnapshotToRpcResponse() {
        PermissionQueryDomainService domainService = mock(PermissionQueryDomainService.class);
        PermissionApplicationServiceImpl service =
                new PermissionApplicationServiceImpl(domainService, new PermissionConverter());
        PermissionSnapshot snapshot = new PermissionSnapshot(7L, "banana", List.of(
                new PermissionResource("banana:home.page", PermissionResourceTypeEnum.PAGE),
                new PermissionResource("banana:admin.oss.add.button", PermissionResourceTypeEnum.BUTTON),
                new PermissionResource("banana:POST:/api/admin/oss", PermissionResourceTypeEnum.API)
        ), 3L, 5L);
        when(domainService.getPermissionSnapshot(7L, "banana")).thenReturn(snapshot);

        PermissionSnapshotResponse result = service.getPermissionSnapshot(
                PermissionSnapshotRequest.builder().userId(7L).systemCode("banana").build());

        assertEquals(List.of("banana:home.page"), result.getPageCodes());
        assertEquals(List.of("banana:admin.oss.add.button"), result.getButtonCodes());
        assertEquals(List.of("banana:POST:/api/admin/oss"), result.getApiCodes());
        assertEquals(3L, result.getUserPermissionVersion());
        assertEquals(5L, result.getSystemPermissionVersion());
    }

    @Test
    void shouldDelegateApiPermissionDecisionToDomainService() {
        PermissionQueryDomainService domainService = mock(PermissionQueryDomainService.class);
        PermissionApplicationServiceImpl service =
                new PermissionApplicationServiceImpl(domainService, new PermissionConverter());
        when(domainService.hasApiPermission(
                7L, "watermelon", "watermelon:GET:/api/admin/users")).thenReturn(true);

        assertTrue(service.hasApiPermission(
                7L, "watermelon", "watermelon:GET:/api/admin/users"));
        assertFalse(service.hasApiPermission(
                7L, "watermelon", "watermelon:admin.users.page"));
    }
}

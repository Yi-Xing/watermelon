package top.fblue.watermelon.application.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.fblue.watermelon.application.converter.RoleConverter;
import top.fblue.watermelon.application.dto.UpdateRoleResourceDTO;
import top.fblue.watermelon.auth.domain.permission.service.PermissionChangeDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleApplicationServiceImplTest {

    @Mock
    private RoleDomainService roleDomainService;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private RoleConverter roleConverter;

    @Mock
    private ResourceDomainService resourceDomainService;

    @Mock
    private PermissionChangeDomainService permissionChangeDomainService;

    @InjectMocks
    private RoleApplicationServiceImpl service;

    @Test
    void shouldNotifyOnlyUsersAssociatedWithRoleWhenResourcesChange() {
        UpdateRoleResourceDTO request = new UpdateRoleResourceDTO();
        request.setId(2L);
        request.setResourceIds(List.of(82L, 83L));
        List<Long> affectedUserIds = List.of(7L, 9L);
        when(userDomainService.getUserIdsByRoleId(2L)).thenReturn(affectedUserIds);
        when(roleDomainService.updateRoleResource(2L, request.getResourceIds())).thenReturn(true);

        boolean result = service.updateRoleResource(request);

        assertTrue(result);
        verify(permissionChangeDomainService).recordUserPermissionChanges(affectedUserIds);
        verify(permissionChangeDomainService, never()).recordSystemPermissionChange();
    }

    @Test
    void shouldCaptureAffectedUsersBeforeDeletingRole() {
        List<Long> affectedUserIds = List.of(7L, 9L);
        when(userDomainService.getUserIdsByRoleId(2L)).thenReturn(affectedUserIds);
        when(roleDomainService.deleteRole(2L)).thenReturn(true);

        boolean result = service.deleteRole(2L);

        assertTrue(result);
        InOrder inOrder = inOrder(userDomainService, roleDomainService, permissionChangeDomainService);
        inOrder.verify(userDomainService).getUserIdsByRoleId(2L);
        inOrder.verify(roleDomainService).deleteRole(2L);
        inOrder.verify(permissionChangeDomainService).recordUserPermissionChanges(affectedUserIds);
        verify(permissionChangeDomainService, never()).recordSystemPermissionChange();
    }
}

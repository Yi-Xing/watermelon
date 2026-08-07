package top.fblue.watermelon.auth.application.service.impl;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeStatusEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeTypeEnum;
import top.fblue.watermelon.auth.domain.permission.service.PermissionChangeDomainService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionChangeApplicationServiceImplTest {

    @Test
    void shouldDispatchOnlyRecordsClaimedByCurrentInstance() {
        PermissionChangeDomainService domainService = mock(PermissionChangeDomainService.class);
        PermissionChangeApplicationServiceImpl applicationService =
                new PermissionChangeApplicationServiceImpl(domainService);
        PermissionChangeRecord claimed = record(1L, "event-1");
        PermissionChangeRecord claimedByOtherInstance = record(2L, "event-2");
        when(domainService.findDispatchableChanges(20))
                .thenReturn(List.of(claimed, claimedByOtherInstance));
        when(domainService.tryClaim(claimed)).thenReturn(true);
        when(domainService.tryClaim(claimedByOtherInstance)).thenReturn(false);

        int result = applicationService.processPendingChanges(20);

        assertEquals(1, result);
        verify(domainService).dispatch(claimed);
        verify(domainService, never()).dispatch(claimedByOtherInstance);
    }

    private PermissionChangeRecord record(Long id, String eventId) {
        return new PermissionChangeRecord(
                id, eventId, PermissionChangeTypeEnum.USER, 7L,
                PermissionChangeStatusEnum.PENDING, 0);
    }
}

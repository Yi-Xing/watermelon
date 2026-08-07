package top.fblue.watermelon.auth.domain.permission.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeStatusEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeTypeEnum;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionChangeRecordRepository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionNotificationRepository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionVersionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionChangeDomainServiceImplTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-07T08:00:00Z"), ZONE_ID);
    private static final LocalDateTime FIXED_TIME = LocalDateTime.now(FIXED_CLOCK);

    @Test
    void shouldRecordDistinctUserChangesInTransactionalOutbox() {
        PermissionChangeRecordRepository recordRepository = mock(PermissionChangeRecordRepository.class);
        PermissionChangeDomainServiceImpl domainService = service(recordRepository);

        domainService.recordUserPermissionChanges(Arrays.asList(7L, null, 7L, 9L));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PermissionChangeRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(recordRepository).saveAll(captor.capture());
        List<PermissionChangeRecord> records = captor.getValue();
        assertEquals(List.of(7L, 9L), records.stream().map(PermissionChangeRecord::userId).toList());
        assertEquals(List.of(PermissionChangeTypeEnum.USER, PermissionChangeTypeEnum.USER),
                records.stream().map(PermissionChangeRecord::changeType).toList());
        assertNotEquals(records.get(0).eventId(), records.get(1).eventId());
    }

    @Test
    void shouldIncrementUserVersionNotifyAndMarkSucceeded() {
        PermissionChangeRecordRepository recordRepository = mock(PermissionChangeRecordRepository.class);
        PermissionVersionRepository versionRepository = mock(PermissionVersionRepository.class);
        PermissionNotificationRepository notificationRepository = mock(PermissionNotificationRepository.class);
        PermissionChangeDomainServiceImpl domainService = service(
                recordRepository, versionRepository, notificationRepository);
        PermissionChangeRecord record = record(1L, "event-1", PermissionChangeTypeEnum.USER, 7L, 0);
        when(versionRepository.incrementUserPermissionVersion("event-1", 7L)).thenReturn(4L);
        when(notificationRepository.findNotifiableSystemCodes()).thenReturn(List.of("banana"));
        when(versionRepository.getSystemPermissionVersion("banana")).thenReturn(5L);

        domainService.dispatch(record);

        InOrder inOrder = inOrder(versionRepository, notificationRepository, recordRepository);
        inOrder.verify(versionRepository).incrementUserPermissionVersion("event-1", 7L);
        inOrder.verify(notificationRepository).findNotifiableSystemCodes();
        inOrder.verify(versionRepository).getSystemPermissionVersion("banana");
        inOrder.verify(notificationRepository).notifyUserChanged(
                "banana", "event-1", 7L, 4L, 5L);
        inOrder.verify(recordRepository).markSucceeded(1L, FIXED_TIME);
    }

    @Test
    void shouldScheduleRetryWhenNotificationFails() {
        PermissionChangeRecordRepository recordRepository = mock(PermissionChangeRecordRepository.class);
        PermissionVersionRepository versionRepository = mock(PermissionVersionRepository.class);
        PermissionNotificationRepository notificationRepository = mock(PermissionNotificationRepository.class);
        PermissionChangeDomainServiceImpl domainService = service(
                recordRepository, versionRepository, notificationRepository);
        PermissionChangeRecord record = record(1L, "event-1", PermissionChangeTypeEnum.USER, 7L, 0);
        when(versionRepository.incrementUserPermissionVersion("event-1", 7L)).thenReturn(4L);
        when(notificationRepository.findNotifiableSystemCodes()).thenReturn(List.of("banana"));
        when(versionRepository.getSystemPermissionVersion("banana")).thenReturn(5L);
        org.mockito.Mockito.doThrow(new IllegalStateException("banana unavailable"))
                .when(notificationRepository).notifyUserChanged(
                        "banana", "event-1", 7L, 4L, 5L);

        domainService.dispatch(record);

        verify(recordRepository).markFailed(
                eq(1L), eq(1), eq(FIXED_TIME.plusSeconds(1)),
                eq("banana unavailable"), eq(FIXED_TIME));
        verify(recordRepository, never()).markSucceeded(eq(1L), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotifySystemChangeWithStableEventId() {
        PermissionChangeRecordRepository recordRepository = mock(PermissionChangeRecordRepository.class);
        PermissionVersionRepository versionRepository = mock(PermissionVersionRepository.class);
        PermissionNotificationRepository notificationRepository = mock(PermissionNotificationRepository.class);
        PermissionChangeDomainServiceImpl domainService = service(
                recordRepository, versionRepository, notificationRepository);
        PermissionChangeRecord record = record(2L, "event-2", PermissionChangeTypeEnum.SYSTEM, null, 0);
        when(notificationRepository.findNotifiableSystemCodes()).thenReturn(List.of("banana"));
        when(versionRepository.incrementSystemPermissionVersion("event-2", "banana")).thenReturn(6L);

        domainService.dispatch(record);

        verify(notificationRepository).notifySystemChanged("banana", "event-2", 6L);
        verify(recordRepository).markSucceeded(2L, FIXED_TIME);
    }

    @Test
    void shouldUseRepositoryToFindAndClaimDispatchableChanges() {
        PermissionChangeRecordRepository recordRepository = mock(PermissionChangeRecordRepository.class);
        PermissionChangeDomainServiceImpl domainService = service(recordRepository);
        PermissionChangeRecord record = record(1L, "event-1", PermissionChangeTypeEnum.USER, 7L, 0);
        when(recordRepository.findDispatchable(
                FIXED_TIME, FIXED_TIME.minusMinutes(1), 20)).thenReturn(List.of(record));
        when(recordRepository.tryMarkProcessing(
                1L, FIXED_TIME, FIXED_TIME.minusMinutes(1))).thenReturn(true);

        assertEquals(List.of(record), domainService.findDispatchableChanges(20));
        assertTrue(domainService.tryClaim(record));
    }

    private PermissionChangeDomainServiceImpl service(PermissionChangeRecordRepository recordRepository) {
        return service(
                recordRepository,
                mock(PermissionVersionRepository.class),
                mock(PermissionNotificationRepository.class));
    }

    private PermissionChangeDomainServiceImpl service(
            PermissionChangeRecordRepository recordRepository,
            PermissionVersionRepository versionRepository,
            PermissionNotificationRepository notificationRepository) {
        return new PermissionChangeDomainServiceImpl(
                recordRepository, versionRepository, notificationRepository, FIXED_CLOCK);
    }

    private PermissionChangeRecord record(Long id,
                                          String eventId,
                                          PermissionChangeTypeEnum type,
                                          Long userId,
                                          int retryCount) {
        return new PermissionChangeRecord(
                id, eventId, type, userId, PermissionChangeStatusEnum.PENDING, retryCount);
    }
}

package top.fblue.watermelon.auth.infrastructure.repository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.infrastructure.converter.PermissionChangeRecordConverter;
import top.fblue.watermelon.auth.infrastructure.mapper.PermissionChangeRecordMapper;
import top.fblue.watermelon.auth.infrastructure.po.PermissionChangeRecordPO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySqlPermissionChangeRecordRepositoryImplTest {

    @Test
    void shouldConvertAndSavePermissionChangeRecord() {
        PermissionChangeRecordMapper mapper = mock(PermissionChangeRecordMapper.class);
        PermissionChangeRecordConverter converter = new PermissionChangeRecordConverter();
        MySqlPermissionChangeRecordRepositoryImpl repository =
                new MySqlPermissionChangeRecordRepositoryImpl(mapper, converter);
        PermissionChangeRecord record = PermissionChangeRecord.pendingUserChange("event-1", 7L);
        when(mapper.insertBatch(anyList())).thenReturn(1);

        repository.saveAll(List.of(record));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PermissionChangeRecordPO>> recordsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mapper).insertBatch(recordsCaptor.capture());
        PermissionChangeRecordPO recordPO = recordsCaptor.getValue().getFirst();
        assertEquals("event-1", recordPO.getEventId());
        assertEquals(7L, recordPO.getUserId());
    }
}

package top.fblue.watermelon.auth.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.infrastructure.po.PermissionChangeRecordPO;

import java.util.List;

/**
 * 权限变更领域记录与持久化对象转换器。
 */
@Component
public class PermissionChangeRecordConverter {

    /**
     * 将领域记录转换为持久化对象。
     *
     * @param record 权限变更领域记录
     * @return 权限变更持久化对象
     */
    public PermissionChangeRecordPO toPO(PermissionChangeRecord record) {
        return PermissionChangeRecordPO.builder()
                .id(record.id())
                .eventId(record.eventId())
                .changeType(record.changeType())
                .userId(record.userId())
                .status(record.status())
                .retryCount(record.retryCount())
                .build();
    }

    /**
     * 将持久化对象列表转换为领域记录列表。
     *
     * @param records 权限变更持久化对象列表
     * @return 权限变更领域记录列表
     */
    public List<PermissionChangeRecord> toDomainList(List<PermissionChangeRecordPO> records) {
        return records.stream().map(this::toDomain).toList();
    }

    /**
     * 将持久化对象转换为领域记录。
     *
     * @param record 权限变更持久化对象
     * @return 权限变更领域记录
     */
    private PermissionChangeRecord toDomain(PermissionChangeRecordPO record) {
        return new PermissionChangeRecord(
                record.getId(), record.getEventId(), record.getChangeType(), record.getUserId(),
                record.getStatus(), record.getRetryCount());
    }
}

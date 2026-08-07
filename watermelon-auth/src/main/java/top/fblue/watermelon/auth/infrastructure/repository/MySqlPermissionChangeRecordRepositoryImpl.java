package top.fblue.watermelon.auth.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionChangeRecordRepository;
import top.fblue.watermelon.auth.infrastructure.converter.PermissionChangeRecordConverter;
import top.fblue.watermelon.auth.infrastructure.mapper.PermissionChangeRecordMapper;
import top.fblue.watermelon.auth.infrastructure.po.PermissionChangeRecordPO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MySQL 的权限变更事务发件箱仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class MySqlPermissionChangeRecordRepositoryImpl implements PermissionChangeRecordRepository {

    /** 权限变更记录 Mapper。 */
    private final PermissionChangeRecordMapper permissionChangeRecordMapper;

    /** 权限变更记录转换器。 */
    private final PermissionChangeRecordConverter permissionChangeRecordConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAll(List<PermissionChangeRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // 1. 转换为持久化对象
        List<PermissionChangeRecordPO> recordPOs = records.stream()
                .map(permissionChangeRecordConverter::toPO)
                .toList();

        // 2. 在调用方当前数据库事务中批量保存
        int insertedCount = permissionChangeRecordMapper.insertBatch(recordPOs);
        if (insertedCount != recordPOs.size()) {
            throw new IllegalStateException("权限变更记录保存数量不一致");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PermissionChangeRecord> findDispatchable(LocalDateTime now,
                                                         LocalDateTime processingTimeoutBefore,
                                                         int limit) {
        if (limit <= 0) {
            return List.of();
        }
        // 1. 查询到期或处理超时记录
        List<PermissionChangeRecordPO> records = permissionChangeRecordMapper.selectDispatchable(
                now, processingTimeoutBefore, limit);

        // 2. 转换为领域记录并返回
        return permissionChangeRecordConverter.toDomainList(records);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tryMarkProcessing(Long id, LocalDateTime now, LocalDateTime processingTimeoutBefore) {
        return permissionChangeRecordMapper.markProcessing(id, now, processingTimeoutBefore) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markSucceeded(Long id, LocalDateTime processedTime) {
        if (permissionChangeRecordMapper.markSucceeded(id, processedTime) != 1) {
            throw new IllegalStateException("权限变更记录成功状态更新失败，id=" + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markFailed(Long id,
                           int retryCount,
                           LocalDateTime nextRetryTime,
                           String lastError,
                           LocalDateTime updatedTime) {
        if (permissionChangeRecordMapper.markFailed(
                id, retryCount, nextRetryTime, lastError, updatedTime) != 1) {
            throw new IllegalStateException("权限变更记录失败状态更新失败，id=" + id);
        }
    }
}

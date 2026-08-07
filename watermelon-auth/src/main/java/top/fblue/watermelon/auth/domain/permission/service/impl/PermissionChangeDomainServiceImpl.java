package top.fblue.watermelon.auth.domain.permission.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeTypeEnum;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionChangeRecordRepository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionNotificationRepository;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionVersionRepository;
import top.fblue.watermelon.auth.domain.permission.service.PermissionChangeDomainService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 权限变更领域服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionChangeDomainServiceImpl implements PermissionChangeDomainService {

    /** 处理中记录超过该时间后允许其他实例重新领取。 */
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(1);

    /** 单次失败重试的最大等待时间。 */
    private static final long MAX_RETRY_DELAY_SECONDS = 300L;

    /** 数据库错误信息字段最大长度。 */
    private static final int MAX_ERROR_LENGTH = 1000;

    /** 权限变更事务发件箱仓储。 */
    private final PermissionChangeRecordRepository permissionChangeRecordRepository;

    /** 权限版本仓储。 */
    private final PermissionVersionRepository permissionVersionRepository;

    /** 业务系统权限缓存失效通知仓储。 */
    private final PermissionNotificationRepository permissionNotificationRepository;

    /** 统一提供可测试的当前时间。 */
    private final Clock clock;

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordUserPermissionChange(Long userId) {
        if (userId == null) {
            return;
        }
        recordUserPermissionChanges(List.of(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordUserPermissionChanges(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        // 1. 过滤空用户并去重，为每个用户创建独立的幂等变更记录
        List<PermissionChangeRecord> records = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(userId -> PermissionChangeRecord.pendingUserChange(
                        UUID.randomUUID().toString(), userId))
                .toList();

        // 2. 与当前权限业务事务一起持久化事务发件箱记录
        permissionChangeRecordRepository.saveAll(records);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordSystemPermissionChange() {
        // 1. 创建系统级权限变更记录
        PermissionChangeRecord record = PermissionChangeRecord.pendingSystemChange(
                UUID.randomUUID().toString());

        // 2. 与当前权限业务事务一起持久化事务发件箱记录
        permissionChangeRecordRepository.saveAll(List.of(record));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PermissionChangeRecord> findDispatchableChanges(int limit) {
        LocalDateTime now = LocalDateTime.now(clock);
        return permissionChangeRecordRepository.findDispatchable(
                now, now.minus(PROCESSING_TIMEOUT), limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tryClaim(PermissionChangeRecord record) {
        LocalDateTime now = LocalDateTime.now(clock);
        return permissionChangeRecordRepository.tryMarkProcessing(
                record.id(), now, now.minus(PROCESSING_TIMEOUT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatch(PermissionChangeRecord record) {
        try {
            // 1. 根据变更范围更新幂等权限版本并通知接入系统
            if (PermissionChangeTypeEnum.USER == record.changeType()) {
                dispatchUserChange(record);
            } else {
                dispatchSystemChange(record);
            }

            // 2. 全部通知成功后标记发件箱记录完成
            permissionChangeRecordRepository.markSucceeded(
                    record.id(), LocalDateTime.now(clock));
        } catch (RuntimeException exception) {
            // 3. 保存失败次数和指数退避时间，等待后台任务再次处理
            markDispatchFailed(record, exception);
        }
    }

    /**
     * 下发用户级权限变更。
     *
     * @param record 用户级权限变更记录
     */
    private void dispatchUserChange(PermissionChangeRecord record) {
        long userPermissionVersion = permissionVersionRepository.incrementUserPermissionVersion(
                record.eventId(), record.userId());
        notifyEachSystem(systemCode -> {
            long systemPermissionVersion =
                    permissionVersionRepository.getSystemPermissionVersion(systemCode);
            permissionNotificationRepository.notifyUserChanged(
                    systemCode, record.eventId(), record.userId(),
                    userPermissionVersion, systemPermissionVersion);
        });
    }

    /**
     * 下发系统级权限变更。
     *
     * @param record 系统级权限变更记录
     */
    private void dispatchSystemChange(PermissionChangeRecord record) {
        notifyEachSystem(systemCode -> {
            long systemPermissionVersion = permissionVersionRepository.incrementSystemPermissionVersion(
                    record.eventId(), systemCode);
            permissionNotificationRepository.notifySystemChanged(
                    systemCode, record.eventId(), systemPermissionVersion);
        });
    }

    /**
     * 通知每个启用系统，并在全部系统尝试完成后汇总抛出失败。
     *
     * @param notification 单个系统通知操作
     */
    private void notifyEachSystem(Consumer<String> notification) {
        RuntimeException notificationFailure = null;
        for (String systemCode : permissionNotificationRepository.findNotifiableSystemCodes()) {
            try {
                notification.accept(systemCode);
            } catch (RuntimeException exception) {
                if (notificationFailure == null) {
                    notificationFailure = exception;
                } else {
                    notificationFailure.addSuppressed(exception);
                }
            }
        }
        if (notificationFailure != null) {
            throw notificationFailure;
        }
    }

    /**
     * 记录下发失败并计算指数退避时间。
     *
     * @param record 当前权限变更记录
     * @param exception 下发异常
     */
    private void markDispatchFailed(PermissionChangeRecord record, RuntimeException exception) {
        int retryCount = record.retryCount() + 1;
        long retryDelaySeconds = Math.min(
                MAX_RETRY_DELAY_SECONDS,
                1L << Math.min(retryCount - 1, 8));
        LocalDateTime now = LocalDateTime.now(clock);
        permissionChangeRecordRepository.markFailed(
                record.id(), retryCount, now.plusSeconds(retryDelaySeconds),
                errorMessage(exception), now);
    }

    /**
     * 提取适合写入数据库的错误摘要。
     *
     * @param exception 下发异常
     * @return 最大一千字符的错误摘要
     */
    private String errorMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getName();
        }
        return message.length() <= MAX_ERROR_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_LENGTH);
    }
}

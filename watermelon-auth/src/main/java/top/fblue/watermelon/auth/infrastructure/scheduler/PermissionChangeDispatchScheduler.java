package top.fblue.watermelon.auth.infrastructure.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.fblue.watermelon.auth.application.service.PermissionChangeApplicationService;

/**
 * 周期扫描并下发权限变更事务发件箱记录。
 */
@Slf4j
@Component
public class PermissionChangeDispatchScheduler {

    /** 权限变更下发应用服务。 */
    private final PermissionChangeApplicationService permissionChangeApplicationService;

    /** 单次最大处理记录数。 */
    private final int batchSize;

    /**
     * 创建权限变更下发调度器。
     *
     * @param permissionChangeApplicationService 权限变更下发应用服务
     * @param batchSize 单次最大处理记录数
     */
    public PermissionChangeDispatchScheduler(
            PermissionChangeApplicationService permissionChangeApplicationService,
            @Value("${permission.change-dispatch.batch-size:20}") int batchSize) {
        this.permissionChangeApplicationService = permissionChangeApplicationService;
        this.batchSize = batchSize;
    }

    /**
     * 扫描并处理一批权限变更；仓储异常会记录日志并等待下次调度重试。
     */
    @Scheduled(
            fixedDelayString = "${permission.change-dispatch.fixed-delay-ms:1000}",
            initialDelayString = "${permission.change-dispatch.initial-delay-ms:1000}")
    public void dispatchPendingChanges() {
        try {
            int processedCount = permissionChangeApplicationService.processPendingChanges(batchSize);
            if (processedCount > 0) {
                log.info("本次处理权限变更记录 {} 条", processedCount);
            }
        } catch (RuntimeException exception) {
            log.error("扫描或处理权限变更记录失败，将在下次调度时重试", exception);
        }
    }
}

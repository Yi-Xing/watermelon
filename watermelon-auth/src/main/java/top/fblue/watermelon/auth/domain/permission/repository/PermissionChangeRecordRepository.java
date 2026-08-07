package top.fblue.watermelon.auth.domain.permission.repository;

import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限变更事务发件箱仓储。
 */
public interface PermissionChangeRecordRepository {

    /**
     * 批量保存待处理权限变更记录。
     *
     * @param records 权限变更记录
     */
    void saveAll(List<PermissionChangeRecord> records);

    /**
     * 查询到期或处理超时的权限变更记录。
     *
     * @param now 当前时间
     * @param processingTimeoutBefore 早于该时间的处理中记录允许重新领取
     * @param limit 最大查询数量
     * @return 待领取的权限变更记录
     */
    List<PermissionChangeRecord> findDispatchable(LocalDateTime now,
                                                  LocalDateTime processingTimeoutBefore,
                                                  int limit);

    /**
     * 原子领取一条权限变更记录。
     *
     * @param id 记录主键
     * @param now 领取时间
     * @param processingTimeoutBefore 处理中记录的超时边界
     * @return 是否领取成功
     */
    boolean tryMarkProcessing(Long id, LocalDateTime now, LocalDateTime processingTimeoutBefore);

    /**
     * 标记权限变更处理成功。
     *
     * @param id 记录主键
     * @param processedTime 完成时间
     */
    void markSucceeded(Long id, LocalDateTime processedTime);

    /**
     * 标记权限变更处理失败并安排下次重试。
     *
     * @param id 记录主键
     * @param retryCount 最新失败次数
     * @param nextRetryTime 下次重试时间
     * @param lastError 最近错误信息
     * @param updatedTime 更新时间
     */
    void markFailed(Long id,
                    int retryCount,
                    LocalDateTime nextRetryTime,
                    String lastError,
                    LocalDateTime updatedTime);
}

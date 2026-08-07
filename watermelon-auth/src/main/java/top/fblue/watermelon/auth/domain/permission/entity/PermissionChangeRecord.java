package top.fblue.watermelon.auth.domain.permission.entity;

/**
 * 持久化到事务发件箱中的权限变更记录。
 *
 * <p>该对象表示创建后不再修改的任务快照，因此使用 Java {@code record}，
 * 不使用会引入 setter 和可变 JavaBean 语义的 Lombok {@code @Data}。</p>
 *
 * @param id 数据库主键；首次保存前为空
 * @param eventId 跨重试保持不变的幂等事件 ID
 * @param changeType 权限变更范围
 * @param userId 受影响用户；系统级变更时为空
 * @param status 当前处理状态
 * @param retryCount 已失败次数
 */
public record PermissionChangeRecord(Long id,
                                     String eventId,
                                     PermissionChangeTypeEnum changeType,
                                     Long userId,
                                     PermissionChangeStatusEnum status,
                                     int retryCount) {

    /**
     * 创建待处理的用户级权限变更记录。
     *
     * @param eventId 幂等事件 ID
     * @param userId 受影响用户 ID
     * @return 待持久化的用户级变更记录
     */
    public static PermissionChangeRecord pendingUserChange(String eventId, Long userId) {
        return new PermissionChangeRecord(
                null, eventId, PermissionChangeTypeEnum.USER, userId,
                PermissionChangeStatusEnum.PENDING, 0);
    }

    /**
     * 创建待处理的系统级权限变更记录。
     *
     * @param eventId 幂等事件 ID
     * @return 待持久化的系统级变更记录
     */
    public static PermissionChangeRecord pendingSystemChange(String eventId) {
        return new PermissionChangeRecord(
                null, eventId, PermissionChangeTypeEnum.SYSTEM, null,
                PermissionChangeStatusEnum.PENDING, 0);
    }
}

package top.fblue.watermelon.auth.domain.permission.entity;

/**
 * 权限变更记录处理状态。
 */
public enum PermissionChangeStatusEnum {
    /** 等待首次处理。 */
    PENDING,
    /** 已被处理实例领取。 */
    PROCESSING,
    /** 已成功更新版本并通知业务系统。 */
    SUCCEEDED,
    /** 处理失败，等待下次重试。 */
    FAILED
}

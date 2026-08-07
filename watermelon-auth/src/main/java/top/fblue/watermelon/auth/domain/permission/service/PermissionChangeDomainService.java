package top.fblue.watermelon.auth.domain.permission.service;

import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;

import java.util.Collection;
import java.util.List;

/**
 * 权限变更领域服务。
 */
public interface PermissionChangeDomainService {

    /**
     * 在当前业务事务中记录指定用户的权限变更。
     *
     * @param userId 受影响用户 ID
     */
    void recordUserPermissionChange(Long userId);

    /**
     * 在当前业务事务中记录指定用户的权限变更。
     *
     * @param userIds 受影响用户 ID 集合
     */
    void recordUserPermissionChanges(Collection<Long> userIds);

    /**
     * 在当前业务事务中记录系统维度权限变更。
     */
    void recordSystemPermissionChange();

    /**
     * 查询等待下发或处理超时的权限变更记录。
     *
     * @param limit 最大查询数量
     * @return 可尝试领取的权限变更记录
     */
    List<PermissionChangeRecord> findDispatchableChanges(int limit);

    /**
     * 原子领取一条权限变更记录。
     *
     * @param record 待领取记录
     * @return 是否领取成功
     */
    boolean tryClaim(PermissionChangeRecord record);

    /**
     * 更新权限版本、通知业务系统并保存处理结果。
     *
     * @param record 已领取的权限变更记录
     */
    void dispatch(PermissionChangeRecord record);
}

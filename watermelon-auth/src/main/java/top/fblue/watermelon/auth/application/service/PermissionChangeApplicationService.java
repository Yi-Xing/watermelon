package top.fblue.watermelon.auth.application.service;

/**
 * 权限变更事务发件箱下发应用服务。
 */
public interface PermissionChangeApplicationService {

    /**
     * 领取并处理一批到期权限变更记录。
     *
     * @param batchSize 单次最大处理数量
     * @return 成功领取的记录数量
     */
    int processPendingChanges(int batchSize);
}

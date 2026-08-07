package top.fblue.watermelon.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.auth.application.service.PermissionChangeApplicationService;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeRecord;
import top.fblue.watermelon.auth.domain.permission.service.PermissionChangeDomainService;

import java.util.List;

/**
 * 权限变更事务发件箱下发应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionChangeApplicationServiceImpl implements PermissionChangeApplicationService {

    /** 权限变更领域服务。 */
    private final PermissionChangeDomainService permissionChangeDomainService;

    /**
     * {@inheritDoc}
     */
    @Override
    public int processPendingChanges(int batchSize) {
        // 1. 通过领域服务查询可处理的权限变更记录
        List<PermissionChangeRecord> records =
                permissionChangeDomainService.findDispatchableChanges(batchSize);

        // 2. 逐条原子领取，只有领取成功的实例可以执行下发
        int claimedCount = 0;
        for (PermissionChangeRecord record : records) {
            if (!permissionChangeDomainService.tryClaim(record)) {
                continue;
            }

            // 3. 通过领域服务更新版本、通知业务系统并记录结果
            permissionChangeDomainService.dispatch(record);
            claimedCount++;
        }

        // 4. 返回本次实际领取数量，便于调度日志和监控
        return claimedCount;
    }
}

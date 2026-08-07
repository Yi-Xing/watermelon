package top.fblue.watermelon.auth.domain.permission.repository;

import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;

import java.util.List;

/**
 * 查询运行时鉴权所需有效资源的仓储端口。
 *
 * <p>接口定义在 Auth 模块，具体资源数据查询由 Watermelon Service 模块实现，
 * 从而保持 {@code watermelon-service -> watermelon-auth} 的单向依赖。</p>
 */
public interface PermissionResourceQueryRepository {

    /**
     * 查询用户在指定系统中的有效页面、按钮和接口资源。
     *
     * @param userId 用户 ID
     * @param systemCode 目标系统编码
     * @return 有效权限资源列表
     */
    List<PermissionResource> findEffectiveResources(Long userId, String systemCode);
}

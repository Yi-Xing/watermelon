package top.fblue.watermelon.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.repository.PermissionResourceQueryRepository;
import top.fblue.watermelon.infrastructure.converter.PermissionResourceConverter;
import top.fblue.watermelon.infrastructure.mapper.ResourceNodeMapper;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;

/**
 * 运行时鉴权资源查询仓储适配器。
 *
 * <p>auth 模块只依赖查询端口；本适配器通过资源 Mapper 读取权限数据，
 * 从而保持 {@code watermelon-service -> watermelon-auth} 的单向依赖，
 * 并避免 Repository 之间相互调用。</p>
 */
@Repository
@RequiredArgsConstructor
public class PermissionResourceQueryRepositoryImpl implements PermissionResourceQueryRepository {

    /** 资源持久化 Mapper。 */
    private final ResourceNodeMapper resourceNodeMapper;

    /** 权限资源模型转换器。 */
    private final PermissionResourceConverter permissionResourceConverter;

    /**
     * 查询用户在目标系统中的有效鉴权资源。
     *
     * @param userId 用户 ID
     * @param systemCode 目标系统编码
     * @return 有效页面、按钮和接口鉴权资源
     */
    @Override
    public List<PermissionResource> findEffectiveResources(Long userId, String systemCode) {
        // 1. 直接通过 Mapper 查询用户拥有的有效资源
        List<ResourceNodePO> resourceNodePOs = resourceNodeMapper.selectEffectiveResources(userId, systemCode);

        // 2. 将持久化对象转换为 auth 模块的运行时鉴权资源并返回
        return permissionResourceConverter.toPermissionResources(resourceNodePOs);
    }
}

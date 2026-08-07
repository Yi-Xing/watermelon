package top.fblue.watermelon.infrastructure.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;
import java.util.Objects;

/**
 * 资源持久化对象与运行时鉴权资源之间的转换器。
 *
 * <p>该转换器位于 service 模块，负责隔离权限数据管理模型与 auth 模块的
 * 运行时鉴权模型，避免 auth 模块反向依赖 service 模块。</p>
 */
@Component
public class PermissionResourceConverter {

    /**
     * 将有效资源节点转换为运行时鉴权资源。
     *
     * <p>目录不参与页面、按钮或接口鉴权，因此转换时会被过滤。</p>
     *
     * @param resourceNodePOs 有效资源节点持久化对象
     * @return 运行时鉴权资源列表
     */
    public List<PermissionResource> toPermissionResources(List<ResourceNodePO> resourceNodePOs) {
        if (resourceNodePOs == null || resourceNodePOs.isEmpty()) {
            return List.of();
        }
        return resourceNodePOs.stream()
                .map(this::toPermissionResource)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 将单个资源节点转换为运行时鉴权资源。
     *
     * @param resourceNodePO 资源节点持久化对象
     * @return 鉴权资源；目录或空节点返回 {@code null}
     */
    private PermissionResource toPermissionResource(ResourceNodePO resourceNodePO) {
        if (resourceNodePO == null) {
            return null;
        }
        PermissionResourceTypeEnum permissionResourceType = toPermissionResourceType(resourceNodePO.getType());
        if (permissionResourceType == null) {
            return null;
        }
        return new PermissionResource(resourceNodePO.getCode(), permissionResourceType);
    }

    /**
     * 将权限管理资源类型转换为运行时鉴权资源类型。
     *
     * @param resourceType 权限管理资源类型编码
     * @return 运行时鉴权资源类型；非鉴权类型返回 {@code null}
     */
    private PermissionResourceTypeEnum toPermissionResourceType(Integer resourceType) {
        if (ResourceTypeEnum.PAGE.getCode().equals(resourceType)) {
            return PermissionResourceTypeEnum.PAGE;
        }
        if (ResourceTypeEnum.BUTTON.getCode().equals(resourceType)) {
            return PermissionResourceTypeEnum.BUTTON;
        }
        if (ResourceTypeEnum.API.getCode().equals(resourceType)) {
            return PermissionResourceTypeEnum.API;
        }
        return null;
    }
}

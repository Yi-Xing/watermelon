package top.fblue.watermelon.auth.domain.permission.entity;

import java.util.List;

/**
 * 用户在单个业务系统中的有效权限快照。
 *
 * <p>该快照创建后不再修改，因此使用 Java {@code record} 表达不可变领域值，
 * 不使用带 setter 的 Lombok {@code @Data}。</p>
 *
 * @param userId 用户 ID
 * @param systemCode 业务系统编码
 * @param resources 有效页面、按钮和接口资源
 * @param userPermissionVersion 用户维度权限版本
 * @param systemPermissionVersion 系统维度权限版本
 */
public record PermissionSnapshot(Long userId,
                                 String systemCode,
                                 List<PermissionResource> resources,
                                 long userPermissionVersion,
                                 long systemPermissionVersion) {

    /**
     * 保证快照中的资源列表不可变且不为空。
     */
    public PermissionSnapshot {
        resources = resources == null ? List.of() : List.copyOf(resources);
    }

    /**
     * 查询指定类型的资源编码，并去重、排序。
     *
     * @param type 资源类型
     * @return 资源编码列表
     */
    public List<String> codesOfType(PermissionResourceTypeEnum type) {
        return resources.stream()
                .filter(resource -> type == resource.type())
                .map(PermissionResource::code)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * 判断是否拥有指定类型和编码的资源权限。
     *
     * @param type 资源类型
     * @param resourceCode 完整资源编码
     * @return 是否拥有权限
     */
    public boolean hasPermission(PermissionResourceTypeEnum type, String resourceCode) {
        return resources.stream()
                .anyMatch(resource -> type == resource.type()
                        && resourceCode.equals(resource.code()));
    }
}

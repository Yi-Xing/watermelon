package top.fblue.watermelon.auth.domain.permission.entity;

/**
 * 参与运行时鉴权的有效资源。
 *
 * <p>该对象只承载创建后不再修改的资源编码和类型，因此使用 Java {@code record}
 * 表达不可变值对象，而不使用会引入可变 JavaBean 语义的 Lombok {@code @Data}。</p>
 *
 * @param code 资源编码
 * @param type 权限资源类型
 */
public record PermissionResource(String code, PermissionResourceTypeEnum type) {
}

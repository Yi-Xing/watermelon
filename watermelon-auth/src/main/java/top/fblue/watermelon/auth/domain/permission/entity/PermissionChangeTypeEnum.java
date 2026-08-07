package top.fblue.watermelon.auth.domain.permission.entity;

/**
 * 权限变更范围。
 */
public enum PermissionChangeTypeEnum {
    /** 指定用户的角色或资源授权发生变化。 */
    USER,
    /** 资源定义等影响整个接入系统的权限策略发生变化。 */
    SYSTEM
}

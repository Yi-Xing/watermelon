CREATE TABLE IF NOT EXISTS role_resource(
    `id` INT AUTO_INCREMENT COMMENT 'ID',
    `role_id` INT NOT NULL COMMENT '角色ID',
    `resource_id` INT NOT NULL COMMENT '资源ID',
    `created_by` INT NOT NULL COMMENT '创建人',
    `created_time` DATETIME NOT NULL COMMENT '创建时间',
    `updated_by` INT NOT NULL COMMENT '更新人',
    `updated_time` DATETIME NOT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除;0 未删除 1 已删除',
    PRIMARY KEY (id)
)  COMMENT = '角色资源关系表';
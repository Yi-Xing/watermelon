CREATE TABLE IF NOT EXISTS resource_relation (
    `id` INT AUTO_INCREMENT COMMENT 'ID',
    `parent_id` INT NOT NULL COMMENT '父级资源ID',
    `child_id` INT NOT NULL COMMENT '子级资源ID',
    `order_num` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `created_by` INT NOT NULL COMMENT '创建人',
    `created_time` DATETIME NOT NULL COMMENT '创建时间',
    `updated_by` INT NOT NULL COMMENT '更新人',
    `updated_time` DATETIME NOT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除;0 未删除 1 已删除',
    PRIMARY KEY (id)
) COMMENT = '资源关系表';
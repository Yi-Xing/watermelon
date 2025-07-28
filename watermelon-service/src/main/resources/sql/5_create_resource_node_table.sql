CREATE TABLE IF NOT EXISTS resource_node(
    `id` INT AUTO_INCREMENT COMMENT '资源ID',
    `name` VARCHAR(20) NOT NULL COMMENT '资源名称',
    `type` TINYINT NOT NULL COMMENT '资源类型;1页面 2按钮 3接口',
    `code` VARCHAR(255) NOT NULL COMMENT '资源code',
    `order_num` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `parent_id` INT NOT NULL DEFAULT 0 COMMENT '父级ID',
    `state` TINYINT NOT NULL DEFAULT 1 COMMENT '状态;1 启用 2 禁用',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    `created_by` INT NOT NULL COMMENT '创建人',
    `created_time` DATETIME NOT NULL COMMENT '创建时间',
    `updated_by` INT NOT NULL COMMENT '更新人',
    `updated_time` DATETIME NOT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除;0 未删除 1 已删除',
    PRIMARY KEY (id)
)  COMMENT = '资源表';
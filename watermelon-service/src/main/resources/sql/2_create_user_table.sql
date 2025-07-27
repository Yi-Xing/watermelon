DROP TABLE IF EXISTS user;
CREATE TABLE user(
                     `id` INT AUTO_INCREMENT COMMENT '用户ID' ,
                     `name` VARCHAR(10) NOT NULL  COMMENT '用户名称' ,
                     `email` VARCHAR(255) NOT NULL  COMMENT '邮箱' ,
                     `phone` VARCHAR(20) NOT NULL  COMMENT '手机号' ,
                     `password` VARCHAR(128) NOT NULL  COMMENT '密码' ,
                     `state` TINYINT(255) NOT NULL  COMMENT '状态;1 启用 2 禁用' ,
                     `remark` VARCHAR(500) NOT NULL  COMMENT '备注' ,
                     `created_by` INT NOT NULL  COMMENT '创建人' ,
                     `created_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                     `updated_by` INT NOT NULL  COMMENT '更新人' ,
                     `updated_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                     `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除;0 未删除 1 已删除' ,
                     PRIMARY KEY (id)
)  COMMENT = '用户表';
CREATE TABLE IF NOT EXISTS permission_change_record (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限变更记录ID',
    `event_id` VARCHAR(64) NOT NULL COMMENT '幂等事件ID',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变更范围：USER/SYSTEM',
    `user_id` BIGINT NULL COMMENT '受影响用户ID，系统级变更为空',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '失败重试次数',
    `next_retry_time` DATETIME(3) NOT NULL COMMENT '下次可处理时间',
    `processing_started_time` DATETIME(3) NULL COMMENT '本次领取时间',
    `processed_time` DATETIME(3) NULL COMMENT '成功处理时间',
    `last_error` VARCHAR(1000) NULL COMMENT '最近失败原因',
    `created_time` DATETIME(3) NOT NULL COMMENT '创建时间',
    `updated_time` DATETIME(3) NOT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_change_event_id` (`event_id`),
    KEY `idx_permission_change_dispatch` (`status`, `next_retry_time`),
    KEY `idx_permission_change_processing` (`status`, `processing_started_time`)
) COMMENT='权限变更事务发件箱';

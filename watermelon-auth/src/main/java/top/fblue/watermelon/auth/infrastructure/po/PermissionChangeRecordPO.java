package top.fblue.watermelon.auth.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeStatusEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionChangeTypeEnum;

import java.time.LocalDateTime;

/**
 * 权限变更事务发件箱持久化对象。
 * 对应数据库表 permission_change_record。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("permission_change_record")
public class PermissionChangeRecordPO {

    /** 权限变更记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 幂等事件 ID。 */
    private String eventId;

    /** 权限变更范围。 */
    private PermissionChangeTypeEnum changeType;

    /** 受影响用户 ID。 */
    private Long userId;

    /** 当前处理状态。 */
    private PermissionChangeStatusEnum status;

    /** 已失败次数。 */
    private Integer retryCount;

    /** 下次可处理时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime nextRetryTime;

    /** 本次领取时间。 */
    private LocalDateTime processingStartedTime;

    /** 成功处理时间。 */
    private LocalDateTime processedTime;

    /** 最近失败原因。 */
    private String lastError;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /** 是否删除：0 未删除，1 已删除。 */
    @TableLogic
    private Integer isDeleted;
}

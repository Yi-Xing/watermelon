package top.fblue.watermelon.auth.infrastructure.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.fblue.watermelon.auth.infrastructure.po.PermissionChangeRecordPO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限变更事务发件箱 Mapper。
 */
@Mapper
public interface PermissionChangeRecordMapper {

    /**
     * 批量插入待处理权限变更记录。
     *
     * @param records 待插入记录
     * @return 插入数量
     */
    @Insert({
            "<script>",
            "INSERT INTO permission_change_record ",
            "(event_id, change_type, user_id, status, retry_count, next_retry_time, ",
            "processing_started_time, processed_time, last_error, created_time, updated_time) ",
            "VALUES ",
            "<foreach collection='records' item='record' separator=','>",
            "(#{record.eventId}, #{record.changeType}, #{record.userId}, #{record.status}, ",
            "#{record.retryCount}, #{record.nextRetryTime}, #{record.processingStartedTime}, ",
            "#{record.processedTime}, #{record.lastError}, #{record.createdTime}, #{record.updatedTime})",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("records") List<PermissionChangeRecordPO> records);

    /**
     * 查询到期或处理超时的权限变更记录。
     *
     * @param now 当前时间
     * @param processingTimeoutBefore 处理超时边界
     * @param limit 最大查询数量
     * @return 可领取记录
     */
    @Select("""
            SELECT id,
                   event_id AS eventId,
                   change_type AS changeType,
                   user_id AS userId,
                   status,
                   retry_count AS retryCount,
                   next_retry_time AS nextRetryTime,
                   processing_started_time AS processingStartedTime,
                   processed_time AS processedTime,
                   last_error AS lastError,
                   created_time AS createdTime,
                   updated_time AS updatedTime,
                   is_deleted AS isDeleted
            FROM permission_change_record
            WHERE is_deleted = 0
              AND ((status IN ('PENDING', 'FAILED') AND next_retry_time <= #{now})
               OR (status = 'PROCESSING' AND processing_started_time <= #{processingTimeoutBefore}))
            ORDER BY id
            LIMIT #{limit}
            """)
    List<PermissionChangeRecordPO> selectDispatchable(@Param("now") LocalDateTime now,
                                                      @Param("processingTimeoutBefore") LocalDateTime processingTimeoutBefore,
                                                      @Param("limit") int limit);

    /**
     * 原子领取待处理或已超时记录。
     *
     * @param id 记录主键
     * @param now 领取时间
     * @param processingTimeoutBefore 处理超时边界
     * @return 更新数量
     */
    @Update("""
            UPDATE permission_change_record
            SET status = 'PROCESSING',
                processing_started_time = #{now},
                updated_time = #{now}
            WHERE id = #{id}
              AND is_deleted = 0
              AND ((status IN ('PENDING', 'FAILED') AND next_retry_time <= #{now})
                OR (status = 'PROCESSING' AND processing_started_time <= #{processingTimeoutBefore}))
            """)
    int markProcessing(@Param("id") Long id,
                       @Param("now") LocalDateTime now,
                       @Param("processingTimeoutBefore") LocalDateTime processingTimeoutBefore);

    /**
     * 标记权限变更记录处理成功。
     *
     * @param id 记录主键
     * @param processedTime 完成时间
     * @return 更新数量
     */
    @Update("""
            UPDATE permission_change_record
            SET status = 'SUCCEEDED',
                processing_started_time = NULL,
                processed_time = #{processedTime},
                last_error = NULL,
                updated_time = #{processedTime}
            WHERE id = #{id}
              AND status = 'PROCESSING'
              AND is_deleted = 0
            """)
    int markSucceeded(@Param("id") Long id, @Param("processedTime") LocalDateTime processedTime);

    /**
     * 标记权限变更处理失败并安排重试。
     *
     * @param id 记录主键
     * @param retryCount 最新失败次数
     * @param nextRetryTime 下次重试时间
     * @param lastError 最近错误摘要
     * @param updatedTime 更新时间
     * @return 更新数量
     */
    @Update("""
            UPDATE permission_change_record
            SET status = 'FAILED',
                retry_count = #{retryCount},
                next_retry_time = #{nextRetryTime},
                processing_started_time = NULL,
                last_error = #{lastError},
                updated_time = #{updatedTime}
            WHERE id = #{id}
              AND status = 'PROCESSING'
              AND is_deleted = 0
            """)
    int markFailed(@Param("id") Long id,
                   @Param("retryCount") int retryCount,
                   @Param("nextRetryTime") LocalDateTime nextRetryTime,
                   @Param("lastError") String lastError,
                   @Param("updatedTime") LocalDateTime updatedTime);
}

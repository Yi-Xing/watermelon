package top.fblue.watermelon.infrastructure.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import top.fblue.auth.context.SsoHttpContext;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 自动填充元数据处理器
 * 在插入和更新数据时自动填充指定字段
 */
@Component
@RequiredArgsConstructor
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    /** Water 提供的应用统一时钟。 */
    private final Clock clock;

    /**
     * 插入数据时填充创建时间、更新时间及当前操作用户。
     *
     * @param metaObject MyBatis 实体元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 1. 同一次插入复用同一个应用时间
        LocalDateTime now = LocalDateTime.now(clock);
        strictInsertFillIfPresent(metaObject, "nextRetryTime", LocalDateTime.class, now);
        strictInsertFillIfPresent(metaObject, "createdTime", LocalDateTime.class, now);
        strictInsertFillIfPresent(metaObject, "updatedTime", LocalDateTime.class, now);

        // 2. 仅对包含操作人字段的普通业务 PO 填充当前用户
        if (metaObject.hasSetter("createdBy") || metaObject.hasSetter("updatedBy")) {
            Long currentUserId = getCurrentUserId();
            strictInsertFillIfPresent(metaObject, "createdBy", Long.class, currentUserId);
            strictInsertFillIfPresent(metaObject, "updatedBy", Long.class, currentUserId);
        }
    }

    /**
     * 更新数据时填充更新时间及当前操作用户。
     *
     * @param metaObject MyBatis 实体元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 1. 使用应用统一时钟填充更新时间
        strictUpdateFillIfPresent(
                metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now(clock));

        // 2. 仅对包含更新人字段的普通业务 PO 填充当前用户
        if (metaObject.hasSetter("updatedBy")) {
            strictUpdateFillIfPresent(metaObject, "updatedBy", Long.class, getCurrentUserId());
        }
    }

    /**
     * 仅在持久化对象包含目标属性时执行插入填充。
     *
     * @param metaObject 持久化对象元数据
     * @param fieldName 字段名称
     * @param fieldType 字段类型
     * @param fieldValue 填充值
     * @param <T> 字段类型
     */
    private <T> void strictInsertFillIfPresent(MetaObject metaObject,
                                               String fieldName,
                                               Class<T> fieldType,
                                               T fieldValue) {
        if (metaObject.hasSetter(fieldName)) {
            strictInsertFill(metaObject, fieldName, fieldType, fieldValue);
        }
    }

    /**
     * 仅在持久化对象包含目标属性时执行更新填充。
     *
     * @param metaObject 持久化对象元数据
     * @param fieldName 字段名称
     * @param fieldType 字段类型
     * @param fieldValue 填充值
     * @param <T> 字段类型
     */
    private <T> void strictUpdateFillIfPresent(MetaObject metaObject,
                                               String fieldName,
                                               Class<T> fieldType,
                                               T fieldValue) {
        if (metaObject.hasSetter(fieldName)) {
            strictUpdateFill(metaObject, fieldName, fieldType, fieldValue);
        }
    }

    /**
     * 获取当前 HTTP 请求中已认证的用户 ID。
     *
     * @return 当前用户 ID
     */
    private Long getCurrentUserId() {
        return SsoHttpContext.getCurrentUserId();
    }
}

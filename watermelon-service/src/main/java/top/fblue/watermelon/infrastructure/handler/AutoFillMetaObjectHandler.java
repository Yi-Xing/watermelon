package top.fblue.watermelon.infrastructure.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import top.fblue.auth.context.SsoHttpContext;

import java.time.LocalDateTime;

/**
 * 自动填充元数据处理器
 * 在插入和更新数据时自动填充指定字段
 */
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入数据时填充创建时间、更新时间及当前操作用户。
     *
     * @param metaObject MyBatis 实体元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        // 这里可以从SecurityContext获取当前用户ID
        this.strictInsertFill(metaObject, "createdBy", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
    }

    /**
     * 更新数据时填充更新时间及当前操作用户。
     *
     * @param metaObject MyBatis 实体元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
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

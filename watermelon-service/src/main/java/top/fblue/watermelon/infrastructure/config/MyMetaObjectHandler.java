package top.fblue.watermelon.infrastructure.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 在插入和更新数据时自动填充指定字段
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        // 这里可以从SecurityContext获取当前用户ID
        this.strictInsertFill(metaObject, "createdBy", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        // 从SecurityContext获取当前用户ID mi:RequestContext.getRequestContext().getUserName()
        // 这里需要根据你的认证方式来实现
        return 1L; // 临时返回1L，实际应该从认证上下文获取
    }
}
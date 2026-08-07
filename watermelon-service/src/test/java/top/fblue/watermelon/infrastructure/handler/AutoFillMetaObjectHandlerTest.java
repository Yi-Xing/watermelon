package top.fblue.watermelon.infrastructure.handler;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.Test;
import top.fblue.watermelon.auth.infrastructure.po.PermissionChangeRecordPO;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoFillMetaObjectHandlerTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-07T08:00:00Z"), ZONE_ID);
    private static final LocalDateTime FIXED_TIME = LocalDateTime.now(FIXED_CLOCK);

    @Test
    void shouldFillTimeWithoutOperatorFields() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        assistant.setCurrentNamespace("test");
        TableInfoHelper.initTableInfo(assistant, PermissionChangeRecordPO.class);
        PermissionChangeRecordPO recordPO = new PermissionChangeRecordPO();
        MetaObject metaObject = configuration.newMetaObject(recordPO);
        AutoFillMetaObjectHandler handler = new AutoFillMetaObjectHandler(FIXED_CLOCK);

        assertDoesNotThrow(() -> handler.insertFill(metaObject));

        assertEquals(FIXED_TIME, recordPO.getNextRetryTime());
        assertEquals(FIXED_TIME, recordPO.getCreatedTime());
        assertEquals(FIXED_TIME, recordPO.getUpdatedTime());
    }
}

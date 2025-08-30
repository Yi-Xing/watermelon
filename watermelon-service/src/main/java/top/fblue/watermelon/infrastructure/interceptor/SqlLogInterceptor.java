package top.fblue.watermelon.infrastructure.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * SQL性能监控拦截器
 * 拦截SQL执行，监控执行时间和性能，打印可执行的SQL语句
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SqlLogInterceptor implements Interceptor {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取执行信息
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        String methodName = invocation.getMethod().getName();
        // 获取SQL语句
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);

        try {
            // 执行原始方法
            Object result = invocation.proceed();

            // 因为 updated_time、updated_by 等字段通过 MetaObjectHandler 填充，所以拼接 sql 要在 proceed 之后
            String sql = getSql(boundSql, parameter, mappedStatement.getConfiguration());

            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            String durationStr = formatDuration(duration);

            // 打印SQL执行信息
            printSqlExecution(methodName, sql, durationStr, result);

            return result;

        } catch (Exception e) {
            String sql = getSql(boundSql, parameter, mappedStatement.getConfiguration());
            // 打印错误信息
            printSqlError(sql, e);
            throw e;
        }
    }

    /**
     * 获取可执行的SQL语句（参数已替换）
     */
    private String getSql(BoundSql boundSql, Object parameter, Configuration configuration) {
        String sql = boundSql.getSql();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return sql;
        }

        try {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = configuration.newMetaObject(parameter);

            for (ParameterMapping parameterMapping : parameterMappings) {
                // 处理所有参数映射
                Object value;
                String propertyName = parameterMapping.getProperty();

                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameter == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
                    value = parameter;
                } else {
                    value = metaObject.getValue(propertyName);
                }

                // 替换参数
                sql = sql.replaceFirst("\\?", formatParameter(value));
            }
        } catch (Exception e) {
            log.error("格式化SQL参数时出错:sql{},e {}", sql, e.getMessage(), e);
        }

        return sql;
    }

    /**
     * 格式化参数值
     */
    private String formatParameter(Object value) {
        return switch (value) {
            case null -> "NULL";
            case String ignored -> "'" + value + "'";
            case Date date -> "'" + TIME_FORMAT.format(date) + "'";
            default -> value.toString();
        };
    }

    /**
     * 格式化执行时间
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 1) {
            return "0ms";
        } else if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.3fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    /**
     * 打印SQL执行信息
     */
    private void printSqlExecution(String methodName, String sql, String duration, Object result) {
        // 日志文件输出
        if ("query".equals(methodName)) {
            if (result instanceof List) {
                log.info("[{}] - count: {} - SQL: {}",
                        duration, ((List<?>) result).size(), sql);
                return;
            }
        }
        log.info("[{}] - count: {} - SQL: {}",
                duration, result, sql);
    }

    /**
     * 打印SQL错误信息
     */
    private void printSqlError(String sql, Exception e) {
        // 日志文件输出
        log.error("SQL ERROR : {} - e: {}", sql, e.getMessage(), e);
    }
}

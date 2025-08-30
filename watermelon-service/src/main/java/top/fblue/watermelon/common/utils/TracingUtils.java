package top.fblue.watermelon.common.utils;

import lombok.extern.slf4j.Slf4j;
import top.fblue.watermelon.common.constant.TraceConst;
import top.fblue.watermelon.common.context.TraceContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全链路追踪工具类
 * 提供追踪相关的实用方法
 *
 * @author system
 */
@Slf4j
public class TracingUtils {

    /**
     * 从HTTP请求中提取追踪上下文并设置到MDC
     *
     * @param request HTTP请求
     */
    public static void extractAndSetTraceContext(HttpServletRequest request) {
        String traceParent = request.getHeader(TraceConst.TRACE_PARENT_HEADER);
        TraceContext traceContext = fromTraceParent(traceParent);
        traceContext.setToMDC();
    }

    /**
     * 从 traceparent header 解析追踪上下文，并生成新的 TraceContext
     * 格式: 00-<trace-id>-<span-id>-<trace-flags>
     *
     * @param traceParent traceParent header值
     * @return TraceContext实例
     */
    private static TraceContext fromTraceParent(String traceParent) {
        if (traceParent == null || traceParent.trim().isEmpty()) {
            // 没有则去生成
            return new TraceContext();
        }
        String[] parts = traceParent.split("-");
        if (parts.length >= 4) {
            String version = parts[0];
            String traceId = parts[1];
            String spanId = parts[2];
            String traceFlags = parts[3];

            // 验证版本号
            if (!TraceConst.TRACE_VERSION.equals(version)) {
                log.warn("Unsupported traceParent version: {}, using default", version);
                return new TraceContext();
            }

            return new TraceContext(traceId, spanId, traceFlags);
        }
        return new TraceContext();
    }


    /**
     * 清除当前线程的追踪上下文
     */
    public static void clearTraceContext() {
        TraceContext.clearFromMDC();
    }
}
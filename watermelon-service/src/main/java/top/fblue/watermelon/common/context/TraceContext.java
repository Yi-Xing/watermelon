package top.fblue.watermelon.common.context;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import top.fblue.watermelon.common.constant.TraceConst;
import top.fblue.watermelon.common.utils.StringUtil;

import java.util.UUID;

/**
 * 全链路追踪上下文
 * 基于 W3C Trace Context 标准实现
 *
 * @author system
 */
@Slf4j
@Data
public class TraceContext {

    /**
     * 追踪ID（Trace ID）
     * 全局唯一，用于标识整个请求链路
     */
    private String traceId;

    /**
     * 父级Span ID（Parent Span ID）
     * 用于标识当前Span的父级Span
     */
    private String parentSpanId;

    /**
     * 当前Span ID
     * 用于标识当前操作
     */
    private String spanId;

    /**
     * 采样标志（Sampled Flag）
     * 00: 不采样, 01: 采样
     */
    private String sampled;


    /**
     * 默认构造函数
     */
    public TraceContext() {
        this.traceId = generateTraceId();
        this.spanId = generateSpanId();
        this.sampled = TraceConst.DEFAULT_SAMPLED;
    }

    /**
     * 构造函数
     *
     * @param traceId      追踪ID
     * @param parentSpanId 父级Span ID
     * @param sampled      采样标志
     */
    public TraceContext(String traceId, String parentSpanId, String sampled) {
        this.traceId = StringUtil.isNotEmpty(traceId) ? traceId : generateTraceId();
        this.parentSpanId = parentSpanId;
        this.spanId = generateSpanId();
        this.sampled = StringUtil.isNotEmpty(sampled) ? sampled : TraceConst.DEFAULT_SAMPLED;
    }

    /**
     * 生成traceparent header值
     * 格式: 00-<trace-id>-<span-id>-<trace-flags>
     *
     * @return traceparent header值
     */
    public String toTraceParent() {
        return String.format("%s-%s-%s-%s", TraceConst.TRACE_VERSION, traceId, spanId, sampled);
    }

    /**
     * 将追踪信息设置到MDC中
     */
    public void setToMDC() {
        MDC.put(TraceConst.TRACE_ID_KEY, traceId);
        MDC.put(TraceConst.SPAN_ID_KEY, spanId);
        if (parentSpanId != null) {
            MDC.put(TraceConst.PARENT_SPAN_ID_KEY, parentSpanId);
        }
        MDC.put(TraceConst.SAMPLED_KEY, sampled);
    }

    /**
     * 从MDC中清除追踪信息
     */
    public static void clearFromMDC() {
        MDC.remove(TraceConst.TRACE_ID_KEY);
        MDC.remove(TraceConst.SPAN_ID_KEY);
        MDC.remove(TraceConst.PARENT_SPAN_ID_KEY);
        MDC.remove(TraceConst.SAMPLED_KEY);
    }

    /**
     * 生成32位追踪ID
     *
     * @return 追踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成16位Span ID
     *
     * @return Span ID
     */
    private String generateSpanId() {
        // 生成一个新的UUID，取前16个字符，去掉横线
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, TraceConst.SPAN_ID_LENGTH);
    }
}

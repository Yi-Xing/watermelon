package top.fblue.watermelon.common.constant;

/**
 * 全链路追踪相关常量
 * 
 * @author system
 */
public class TraceConst {
    
    /**
     * 追踪上下文在MDC中的键名
     */
    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";
    public static final String PARENT_SPAN_ID_KEY = "parentSpanId";
    public static final String SAMPLED_KEY = "sampled";
    
    /**
     * HTTP Header名称常量
     */
    public static final String TRACE_PARENT_HEADER = "traceparent";
    public static final String TRACE_STATE_HEADER = "tracestate";
    
    /**
     * W3C Trace Context 标准常量
     */
    public static final String TRACE_VERSION = "00";
    public static final int TRACE_ID_LENGTH = 32;  // 32个十六进制字符
    public static final int SPAN_ID_LENGTH = 16;   // 16个十六进制字符
    public static final int TRACE_FLAGS_LENGTH = 2; // 2个十六进制字符
    
    /**
     * 采样标志常量
     */
    public static final String SAMPLED_YES = "01";  // 采样
    public static final String SAMPLED_NO = "00";   // 不采样
    
    /**
     * 默认值常量
     */
    public static final String DEFAULT_SAMPLED = SAMPLED_YES;
}

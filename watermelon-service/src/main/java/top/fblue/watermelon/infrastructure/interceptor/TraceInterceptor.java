package top.fblue.watermelon.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.common.utils.TracingUtils;


/**
 * 全链路追踪拦截器
 * 自动处理HTTP请求的追踪上下文
 *
 * @author system
 */
@Slf4j
@Component
public class TraceInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        // 提取并设置追踪上下文
        TracingUtils.extractAndSetTraceContext(request);
        
        // 构建请求入参日志
        String requestMethod = request.getMethod();
        String requestPath = request.getRequestURI();
        String requestIP = getClientIP(request);
        
        // 记录请求入参日志
        log.info("http requestMethod={} requestPath={} requestIP={}",
                requestMethod, requestPath, requestIP);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 获取请求开始时间
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            long endTime = System.currentTimeMillis();
            long latency = startTime != null ? endTime - startTime : 0;
            
            // 获取 http 状态码
            int responseStatusCode = response.getStatus();
            String responseLatency = String.format("%dms", latency);

            // 记录请求出参日志，全局异常处理把异常给拦截了，这里ex没有意义
            log.info("http responseStatusCode={} responseLatency={}",
                    responseStatusCode, responseLatency);
            
        } catch (Exception logEx) {
            // 只是捕获日志记录过程中的异常，不会影响原始业务异常的传递。
            log.error("afterCompletion err", logEx);
        } finally {
            // 清除追踪上下文
            TracingUtils.clearTraceContext();
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

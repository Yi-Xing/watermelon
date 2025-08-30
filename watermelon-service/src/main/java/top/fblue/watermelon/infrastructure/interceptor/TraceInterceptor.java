package top.fblue.watermelon.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 提取并设置追踪上下文
        TracingUtils.extractAndSetTraceContext(request);
        // 记录请求开始日志
        log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 记录请求完成日志
            if (ex != null) {
                log.error("Request completed with error: {} {} - Exception: {}", 
                        request.getMethod(), request.getRequestURI(), ex.getMessage());
            } else {
                log.info("Request completed: {} {} - Status: {}", 
                        request.getMethod(), request.getRequestURI(), response.getStatus());
            }
        } catch (Exception logEx) {
            log.error("Failed to log request completion", logEx);
        } finally {
            // 清除追踪上下文
            TracingUtils.clearTraceContext();
        }
    }
}

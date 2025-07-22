package top.fblue.watermelon.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务异常: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", e.getMessage());
        response.put("success", false);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数验证异常: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", "参数验证失败");
        response.put("success", false);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束违反异常: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", "参数验证失败");
        response.put("success", false);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException e) {
        log.warn("绑定异常: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", "参数绑定失败");
        response.put("success", false);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("系统异常", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "系统内部错误");
        response.put("success", false);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 
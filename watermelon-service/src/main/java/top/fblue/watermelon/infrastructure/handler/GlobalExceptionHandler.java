package top.fblue.watermelon.infrastructure.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.common.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());

        return ApiResponse.error(500, e.getMessage());
    }

    /**
     * 处理参数验证异常 (400 Bad Request)
     * 处理@Valid注解的验证失败，@RequestBody + @Valid 校验失败，JSON请求体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数验证异常: {}", e.getMessage());

        return buildValidationErrorResponse(e.getBindingResult().getFieldErrors());
    }

    /**
     * 处理绑定异常 (400 Bad Request)
     * 处理表单绑定验证失败，@ModelAttribute 类型绑定失败，表单提交或非JSON
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleBindException(BindException e) {
        log.warn("绑定异常: {}", e.getMessage());

        return buildValidationErrorResponse(e.getBindingResult().getFieldErrors());
    }

    /**
     * 处理约束违反异常 (400 Bad Request)
     * 处理@Validated注解的验证失败，@RequestParam/@PathVariable 校验失败，GET或简单POST
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束违反异常: {}", e.getMessage());

        // 提取所有验证错误信息
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        // 添加详细的错误信息
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        ApiResponse<Object> response = ApiResponse.error(400,
                errorMessage.isEmpty() ? "参数验证失败" : errorMessage);
        response.setData(errors);

        return response;
    }

    /**
     * 处理通用异常 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(500, e.getMessage());
    }

    /**
     * 构建字段验证错误响应
     */
    private ApiResponse<Object> buildValidationErrorResponse(java.util.List<FieldError> fieldErrors) {
        // 提取所有验证错误信息
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        // 添加详细的错误信息
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiResponse<Object> response = ApiResponse.error(400,
                errorMessage.isEmpty() ? "参数验证失败" : errorMessage);
        response.setData(errors);

        return response;
    }
} 
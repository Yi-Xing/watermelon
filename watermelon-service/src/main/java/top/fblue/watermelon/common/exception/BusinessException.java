package top.fblue.watermelon.common.exception;

/**
 * 业务异常基类
 * 所有业务相关异常的父类
 */
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * HTTP状态码
     */
    private final int httpStatus;
    
    /**
     * 业务模块
     */
    private final String module;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.httpStatus = 400;
        this.module = "GENERAL";
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.httpStatus = 400;
        this.module = "GENERAL";
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 400;
        this.module = "GENERAL";
    }
    
    public BusinessException(String errorCode, String message, String module) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 400;
        this.module = module;
    }
    
    public BusinessException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.module = "GENERAL";
    }
    
    public BusinessException(String errorCode, String message, String module, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.module = module;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause, String module, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.module = module;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
    
    public String getModule() {
        return module;
    }
}

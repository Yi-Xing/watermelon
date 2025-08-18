package top.fblue.watermelon.common.exception;

/**
 * JWT基础异常类
 * 所有JWT相关异常的父类
 */
public class JwtException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * HTTP状态码
     */
    private final int httpStatus;
    
    public JwtException(String message) {
        super(message);
        this.errorCode = "JWT_ERROR";
        this.httpStatus = 400;
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "JWT_ERROR";
        this.httpStatus = 400;
    }
    
    public JwtException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 400;
    }
    
    public JwtException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public JwtException(String errorCode, String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}

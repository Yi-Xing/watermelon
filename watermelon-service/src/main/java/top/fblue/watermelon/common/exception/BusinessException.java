package top.fblue.watermelon.common.exception;

/**
 * 业务异常基类
 * 所有业务相关异常的父类
 */
public class BusinessException extends RuntimeException {

    public BusinessException() {
        super();
    }


    public BusinessException(String message) {
        super(message);
    }
}

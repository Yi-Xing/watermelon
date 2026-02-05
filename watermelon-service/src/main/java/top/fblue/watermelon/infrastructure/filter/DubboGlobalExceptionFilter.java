//package top.fblue.watermelon.infrastructure.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.common.extension.Activate;
//import org.apache.dubbo.rpc.AsyncRpcResult;
//import org.apache.dubbo.rpc.Filter;
//import org.apache.dubbo.rpc.Invocation;
//import org.apache.dubbo.rpc.Invoker;
//import org.apache.dubbo.rpc.Result;
//
//import top.fblue.watermelon.common.exception.BusinessException;
//import top.fblue.watermelon.common.response.ApiResponse;
//
//import jakarta.validation.ConstraintViolationException;
//
//import java.util.stream.Collectors;
//
//import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;
//
///**
// * Dubbo 统一异常处理 Filter（服务端）
// * 捕获业务异常、参数校验异常等，统一封装为 ApiResponse 错误返回，不向外抛异常
// */
//@Slf4j
//@Activate(group = PROVIDER)
//public class DubboGlobalExceptionFilter implements Filter {
//
//    @Override
//    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RuntimeException {
//        log.info("DubboGlobalExceptionFilter start");
//        try {
//            Result result = invoker.invoke(invocation);
//            if (result.hasException()) {
//                return buildErrorResult(result.getException(), invocation);
//            }
//            return result;
//        } catch (Exception e) {
//            return buildErrorResult(e, invocation);
//        }
//    }
//
//    private Result buildErrorResult(Throwable e, Invocation invocation) {
//        ApiResponse<?> errorResponse = toApiResponse(e);
//        log.warn("Dubbo 调用异常 [{}#{}]，异常信息：{}", invocation.getTargetServiceUniqueName(), invocation.getMethodName(), e.getMessage());
//        String target = invocation.getTargetServiceUniqueName() + "#" + invocation.getMethodName();
//        if (e instanceof BusinessException) {
//            log.error("Dubbo 业务异常 {}:", target, e);
//        } else if (e instanceof ConstraintViolationException) {
//            log.warn("Dubbo 参数校验失败 [{}]: {}", target, e.getMessage());
//        } else {
//            log.error("Dubbo 调用其它异常 [{}]", target, e);
//        }
//        return AsyncRpcResult.newDefaultAsyncResult(errorResponse, invocation);
//    }
//
//    private ApiResponse<?> toApiResponse(Throwable e) {
//        if (e instanceof BusinessException) {
//            return ApiResponse.error(500, e.getMessage());
//        }
//        if (e instanceof ConstraintViolationException cve) {
//            String message = cve.getConstraintViolations().stream()
//                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
//                    .collect(Collectors.joining("; "));
//            return ApiResponse.error(400, message.isEmpty() ? "参数校验失败" : message);
//        }
//        if (e.getCause() != null) {
//            return toApiResponse(e.getCause());
//        }
//        return ApiResponse.error(500, e.getMessage() != null ? e.getMessage() : "系统异常");
//    }
//}

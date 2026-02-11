package top.fblue.watermelon.auth.common.utils;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import top.fblue.watermelon.auth.common.annotation.RpcPublic;

import java.lang.reflect.Method;

/**
 * 与 @RpcPublic 相关的 RPC 工具方法（依赖 Dubbo Invoker/Invocation，故放在 infrastructure 层）
 */
public final class RpcPublicUtils {

    private RpcPublicUtils() {
    }

    /**
     * 判断当前 RPC 调用对应的方法是否标注了 @RpcPublic（无需传递用户信息）
     */
    public static boolean isPublic(Invoker<?> invoker, Invocation invocation) {
        try {
            Class<?> service = invoker.getInterface();
            String methodName = invocation.getMethodName();
            Class<?>[] paramTypes = invocation.getParameterTypes();
            Method method = service.getMethod(methodName, paramTypes);
            return method.isAnnotationPresent(RpcPublic.class);
        } catch (NoSuchMethodException e) {
            // 理论上不可能发生，发生了直接按需要鉴权处理
            return false;
        }
    }
}

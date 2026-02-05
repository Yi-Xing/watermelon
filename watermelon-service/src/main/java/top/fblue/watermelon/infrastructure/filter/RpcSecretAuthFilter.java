package top.fblue.watermelon.infrastructure.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.common.constant.RpcConst;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.infrastructure.config.RpcSecretConfig;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * Dubbo RPC 秘钥鉴权 Filter（服务端）
 * 校验调用方在 RpcContext 中传递的 rpc-secret 与配置的秘钥是否一致。
 * order = -1，在 DubboExceptionFilter(-2) 之后执行，鉴权异常由 ExceptionFilter 统一捕获。
 */
@Slf4j
@Activate(group = PROVIDER)
public class RpcSecretAuthFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("RpcSecretAuthFilter start");
        RpcSecretConfig rpcSecretConfig = RpcSecretConfig.getInstance();
        if (rpcSecretConfig == null || !rpcSecretConfig.isAuthEnabled()) {
            return invoker.invoke(invocation);
        }
        String expectedSecret = rpcSecretConfig.getSecret();
        if (!StringUtils.hasText(expectedSecret)) {
            return invoker.invoke(invocation);
        }
        // 从调用方传递的 attachment 中获取秘钥（消费端通过 RpcContext.getClientAttachment().setAttachment("rpc-secret", secret) 传递）
        String clientSecret = RpcContext.getClientAttachment().getAttachment(RpcConst.SECRET_KEY);
        if (!expectedSecret.equals(clientSecret)) {
            throw new BusinessException("RPC 鉴权失败：秘钥无效或未传递");
        }
        return invoker.invoke(invocation);
    }
}

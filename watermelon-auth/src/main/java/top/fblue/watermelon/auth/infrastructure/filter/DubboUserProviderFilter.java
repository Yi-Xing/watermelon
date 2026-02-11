package top.fblue.watermelon.auth.infrastructure.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.watermelon.auth.common.context.UserRpcContext;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.common.utils.RpcPublicUtils;

@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboUserProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        if (RpcPublicUtils.isPublic(invoker, invocation)) {
            return invoker.invoke(invocation);
        }
        UserDTO userDTO = UserRpcContext.getCurrentUserInfo();
        log.debug("rpc 调用成功，用户ID: {}", userDTO.getUserId());
        return invoker.invoke(invocation);
    }
}


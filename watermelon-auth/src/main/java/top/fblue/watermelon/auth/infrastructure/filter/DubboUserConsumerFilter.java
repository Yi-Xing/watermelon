package top.fblue.watermelon.auth.infrastructure.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.watermelon.auth.common.context.UserContext;
import top.fblue.watermelon.auth.common.dto.UserDTO;
import top.fblue.watermelon.auth.common.utils.RpcPublicUtils;

import static top.fblue.watermelon.auth.common.constant.UserConst.CURRENT_USER_KEY;

@Activate(group = CommonConstants.CONSUMER)
public class DubboUserConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        if (RpcPublicUtils.isPublic(invoker, invocation)) {
            return invoker.invoke(invocation);
        }
        UserDTO userDTO = UserContext.getCurrentUserInfo();
        RpcContext.getClientAttachment().setObjectAttachment(CURRENT_USER_KEY, userDTO);
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.getClientAttachment().removeAttachment(CURRENT_USER_KEY);
        }
    }
}


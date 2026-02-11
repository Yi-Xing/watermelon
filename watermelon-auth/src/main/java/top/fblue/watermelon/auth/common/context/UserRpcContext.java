package top.fblue.watermelon.auth.common.context;

import org.apache.dubbo.rpc.RpcContext;
import top.fblue.common.exception.BusinessException;
import top.fblue.watermelon.auth.common.constant.UserConst;
import top.fblue.watermelon.auth.common.dto.UserDTO;


/**
 * 用户RPC上下文工具类
 * 用于获取当前登录用户信息
 */
public class UserRpcContext {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUserInfo().getUserId();
    }

    /**
     * 获取当前登录用户信息
     */
    public static UserDTO getCurrentUserInfo() {
        Object userToken = RpcContext.getServerAttachment().getObjectAttachment(UserConst.CURRENT_USER_KEY);
        if (userToken == null) {
            throw new BusinessException("RPC 用户上下文不存在");
        }
        return (UserDTO) userToken;
    }
}
package top.fblue.watermelon.common.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.common.exception.BusinessException;

import static top.fblue.watermelon.common.constant.UserConst.CURRENT_USER_KEY;


/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 */
public class UserContext {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUserInfo().getUserId();
    }

    /**
     * 获取当前登录用户信息
     */
    public static UserTokenDTO getCurrentUserInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("用户未登录");
        }

        HttpServletRequest request = attributes.getRequest();
        Object userToken = request.getAttribute(CURRENT_USER_KEY);
        if (userToken == null) {
            throw new BusinessException("用户未登录");
        }
        return (UserTokenDTO) userToken;
    }
}
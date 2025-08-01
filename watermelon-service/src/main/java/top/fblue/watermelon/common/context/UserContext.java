package top.fblue.watermelon.common.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static top.fblue.watermelon.common.constant.UserConst.CURRENT_USER_KEY;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 */
public class UserContext {

    /**
     * 获取当前登录用户
     */
    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        HttpServletRequest request = attributes.getRequest();
        Object userId = request.getAttribute(CURRENT_USER_KEY);
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return (Long) userId;
    }
}
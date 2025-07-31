package top.fblue.watermelon.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.fblue.watermelon.domain.user.entity.User;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 */
public class UserContext {
    
    private static final String CURRENT_USER_KEY = "currentUser";
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        return (User) request.getAttribute(CURRENT_USER_KEY);
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
} 
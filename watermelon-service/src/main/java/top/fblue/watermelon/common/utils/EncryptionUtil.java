package top.fblue.watermelon.common.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * 密码加密工具类
 * 使用纯BCrypt进行加密，支持用户ID加盐
 */
public class EncryptionUtil {
    
    /**
     * BCrypt成本因子，值越高计算越慢但越安全
     * 推荐值：10-12，这里使用10作为平衡点
     */
    private static final int COST_FACTOR = 10;
    
    /**
     * 加密密码，使用用户ID作为盐值的一部分
     * 
     * @param rawPassword 原始密码
     * @param userId 用户ID，用于加盐
     * @return 加密后的密码
     */
    public static String encode(String rawPassword, Long userId) {
        if (StringUtil.isEmpty(rawPassword)) {
            return null;
        }
        
        // 将用户ID转换为字符串，作为盐值的一部分
        String salt = String.valueOf(userId);
        
        // 使用BCrypt加密，BCrypt会自动生成随机盐值
        // 这里我们将用户ID作为额外的盐值信息
        String saltedPassword = rawPassword + "_" + salt;
        
        // 使用纯BCrypt库进行加密
        return BCrypt.withDefaults().hashToString(COST_FACTOR, saltedPassword.toCharArray());
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @param userId 用户ID，用于加盐
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword, Long userId) {
        if (StringUtil.isEmpty(rawPassword) || StringUtil.isEmpty(encodedPassword) || userId == null) {
            return false;
        }
        
        // 将用户ID转换为字符串，作为盐值的一部分
        String salt = String.valueOf(userId);
        String saltedPassword = rawPassword + "_" + salt;
        
        // 使用纯BCrypt库进行验证
        BCrypt.Result result = BCrypt.verifyer().verify(saltedPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}

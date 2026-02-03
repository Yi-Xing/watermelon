package top.fblue.watermelon.dubbo.filter;

/**
 * RPC 秘钥配置持有者
 * 由 RpcSecretConfig 在启动时注入，供 RpcSecretAuthFilter 使用（Filter 为 SPI 加载，无法直接注入 Spring Bean）
 */
public final class RpcSecretHolder {

    /**
     * 调用方需在 RpcContext.getClientAttachment().setAttachment(ATTACHMENT_KEY, secret) 传递的 key
     */
    public static final String ATTACHMENT_KEY = "rpc-secret";

    private static volatile String secret = "";
    private static volatile boolean authEnabled = true;

    public static String getSecret() {
        return secret;
    }

    public static void setSecret(String secret) {
        RpcSecretHolder.secret = secret == null ? "" : secret;
    }

    public static boolean isAuthEnabled() {
        return authEnabled;
    }

    public static void setAuthEnabled(boolean authEnabled) {
        RpcSecretHolder.authEnabled = authEnabled;
    }

    private RpcSecretHolder() {
    }
}

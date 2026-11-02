package cn.miskk.blog.common.auth;

/**
 * 基于 ThreadLocal 的当前用户上下文（由 AuthInterceptor 在请求进入时填充、结束时清理）。
 */
public final class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前用户 ID，未登录（匿名）返回 null；需要登录态的接口由 @RequireLogin 保证前置校验。
     */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

package cn.miskk.blog.common.auth;

import cn.hutool.json.JSONUtil;

/**
 * 基于 ThreadLocal 的当前用户上下文。
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
     * 获取当前用户 ID，未登录抛出业务异常由调用方保证前置校验。
     */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /**
     * 当前上下文序列化为 X-User-Info 请求头值，供网关 Feign 调用下发；
     * 未设置上下文时返回 null，Feign 将省略该请求头（下游视为匿名）。
     */
    public static String toJson() {
        LoginUser user = HOLDER.get();
        return user == null ? null : JSONUtil.toJsonStr(user);
    }

    public static void clear() {
        HOLDER.remove();
    }
}

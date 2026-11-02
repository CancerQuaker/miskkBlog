package cn.miskk.blog.common.constant;

/**
 * 认证相关常量。
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final int ROLE_USER = 0;
    public static final int ROLE_ADMIN = 1;

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
}

package cn.miskk.blog.common.constant;

/**
 * 认证与内部调用相关常量。
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /** 网关解析 JWT 后向下游传递的用户请求头 */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    /** 服务间内部调用鉴权请求头 */
    public static final String HEADER_INTERNAL_TOKEN = "X-Internal-Token";

    public static final int ROLE_USER = 0;
    public static final int ROLE_ADMIN = 1;

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
}

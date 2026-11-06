package cn.miskk.blog.common.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前请求的用户上下文：由 AuthInterceptor 解析 JWT 后构造并写入 {@link UserContext}（ThreadLocal），
 * 业务层通过 {@code UserContext.get()} / {@code UserContext.getUserId()} 读取。
 *
 * <ul>
 *   <li>{@link #userId}：为 null 表示匿名请求（匿名请求也会填充上下文，便于统一取值）；</li>
 *   <li>{@link #role}：0 普通用户 / 1 管理员，匿名固定为 0；</li>
 *   <li>{@link #ip}：客户端真实 IP，从 X-Forwarded-For / remoteAddr 解析，
 *       供浏览量等"按访客去重"类逻辑使用。</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class LoginUser {

    /** 用户 ID，null 表示匿名 */
    private Long userId;

    /** 0 普通用户 1 管理员 */
    private Integer role;

    /** 客户端真实 IP */
    private String ip;

    /** 服务内部构造临时上下文时使用（无 IP 信息，如管理员保存文章后以管理员视角回显详情） */
    public LoginUser(Long userId, Integer role) {
        this.userId = userId;
        this.role = role == null ? 0 : role;
    }

    /** 鉴权拦截器解析 JWT 后构造完整上下文时使用 */
    public LoginUser(Long userId, Integer role, String ip) {
        this.userId = userId;
        this.role = role == null ? 0 : role;
        this.ip = ip;
    }

    public boolean isAdmin() {
        return role != null && role == cn.miskk.blog.common.constant.SecurityConstants.ROLE_ADMIN;
    }
}

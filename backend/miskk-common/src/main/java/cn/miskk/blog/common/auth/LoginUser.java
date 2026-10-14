package cn.miskk.blog.common.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 跨服务用户上下文对象：网关是唯一解析 JWT 的地方，解析结果即为此对象，
 * 需要传递给下游服务时，由网关在 Feign 调用处以请求头参数形式显式下发
 * （userId → X-User-Id，role → X-User-Role，ip → X-Forwarded-For），
 * 下游服务的 AuthInterceptor 收到请求头后还原为本对象。
 *
 * <ul>
 *   <li>{@link #userId}：为 null 表示匿名请求（网关对匿名请求也会填充上下文，便于统一取值）；</li>
 *   <li>{@link #role}：0 普通用户 / 1 管理员，匿名固定为 0；</li>
 *   <li>{@link #ip}：客户端真实 IP，由网关从 X-Forwarded-For / remoteAddr 解析，
 *       供浏览量、观看时长等"按访客去重"类逻辑使用（转发后下游拿不到原始 IP）。</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class LoginUser {

    /** 用户 ID，null 表示匿名 */
    private Long userId;

    /** 0 普通用户 1 管理员 */
    private Integer role;

    /** 客户端真实 IP（网关解析） */
    private String ip;

    /** 下游服务从请求头还原上下文时使用（无 IP 信息） */
    public LoginUser(Long userId, Integer role) {
        this.userId = userId;
        this.role = role == null ? 0 : role;
    }

    /** 网关解析 JWT 后构造完整上下文时使用 */
    public LoginUser(Long userId, Integer role, String ip) {
        this.userId = userId;
        this.role = role == null ? 0 : role;
        this.ip = ip;
    }

    public boolean isAdmin() {
        return role != null && role == cn.miskk.blog.common.constant.SecurityConstants.ROLE_ADMIN;
    }
}

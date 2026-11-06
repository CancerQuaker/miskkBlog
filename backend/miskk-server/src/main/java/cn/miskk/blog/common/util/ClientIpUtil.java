package cn.miskk.blog.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端真实 IP 解析工具：优先取 X-Forwarded-For 首跳（多级代理场景），
 * 缺失或为 unknown 时回退到 TCP 远端地址。
 * <p>
 * AuthInterceptor 在鉴权阶段解析一次并写入 {@link cn.miskk.blog.common.auth.LoginUser#getIp()}，
 * 供浏览量等"按访客去重"类逻辑统一取值。
 */
public final class ClientIpUtil {

    private ClientIpUtil() {
    }

    public static String resolve(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

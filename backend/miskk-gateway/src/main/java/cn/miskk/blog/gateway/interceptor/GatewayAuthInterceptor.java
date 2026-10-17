package cn.miskk.blog.gateway.interceptor;

import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.constant.SecurityConstants;
import cn.miskk.blog.common.util.ClientIpUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关统一鉴权拦截器（原响应式 AuthGlobalFilter 的 MVC 版本）：
 * 1. 解析 Authorization 中的 JWT，校验通过则写入 UserContext；Feign 调用时由 UserContext.toJson()
 *    以单个 X-User-Info 请求头（LoginUser JSON：userId/role/ip）向下游下发用户上下文；
 * 2. 非白名单请求必须携带有效 JWT，否则返回 401；
 * 3. /api/admin/** 仅允许管理员（role=1）访问；
 * 4. 白名单内请求若携带有效 JWT 依然注入用户信息（匿名可浏览、登录有身份）。
 */
@Slf4j
@Component
public class GatewayAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /** 无需登录即可访问的接口（任意方法） */
    private static final List<String> PUBLIC_ALL = List.of(
            "/api/user/auth/login",
            "/api/user/auth/register",
            "/api/user/auth/code",
            "/api/user/auth/oauth/**",
            "/api/article/articles/*/view",
            "/api/article/articles/*/share",
            "/api/article/articles/*/watch",
            "/uploads/**"
    );

    /** 无需登录即可访问的接口（仅 GET） */
    private static final List<String> PUBLIC_GET = List.of(
            "/api/article/articles",
            "/api/article/articles/*",
            "/api/article/articles/*/comments",
            "/api/article/comments",
            "/api/article/categories",
            "/api/article/categories/*",
            "/api/article/tags",
            "/api/article/search",
            "/api/friend/links",
            "/api/friend/apply/notes",
            "/api/static/**",
            "/api/user/users/*",
            // 接口文档（knife4j / springdoc），生产环境可按需收紧
            "/doc.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**"
    );

    public GatewayAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 管理端接口：必须为管理员
        boolean adminPath = matcher.match("/api/admin/**", path);

        String token = null;
        String auth = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (auth != null && auth.startsWith(SecurityConstants.BEARER_PREFIX)) {
            token = auth.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }

        Claims claims = token == null || token.isEmpty() ? null : jwtUtil.parse(token);

        boolean requiresAuth = !isPublic(path, method);
        if (requiresAuth && claims == null) {
            return reject(response, 401, "请先登录后再操作");
        }
        if (adminPath && (claims == null
                || !String.valueOf(SecurityConstants.ROLE_ADMIN)
                .equals(String.valueOf(claims.get(SecurityConstants.CLAIM_ROLE, Integer.class))))) {
            return reject(response, 403, "需要管理员权限");
        }

        // 网关统一填充用户上下文（含匿名请求）：userId 为空表示未登录，
        // ip 供浏览量/观看时长按访客去重；Feign 调用时经 UserContext.toJson() 序列化为 X-User-Info 下发
        Long uid = null;
        Integer role = 0;
        if (claims != null) {
            uid = claims.get(SecurityConstants.CLAIM_USER_ID, Long.class);
            Integer claimRole = claims.get(SecurityConstants.CLAIM_ROLE, Integer.class);
            role = claimRole == null ? 0 : claimRole;
        }
        UserContext.set(new LoginUser(uid, role, ClientIpUtil.resolve(request)));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isPublic(String path, String method) {
        if ("GET".equalsIgnoreCase(method)) {
            for (String pattern : PUBLIC_GET) {
                if (matcher.match(pattern, path)) {
                    return true;
                }
            }
        }
        for (String pattern : PUBLIC_ALL) {
            if (matcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean reject(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
        return false;
    }
}

package cn.miskk.blog.web.interceptor;

import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.RequireAdmin;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.constant.SecurityConstants;
import cn.miskk.blog.common.util.ClientIpUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 统一鉴权拦截器：
 * 1. 解析 Authorization 中的 JWT，解析结果写入 UserContext（匿名请求也填充，userId 为 null，
 *    ip 供浏览量/观看时长按访客去重）；
 * 2. 非白名单请求必须携带有效 JWT，否则返回 401；
 * 3. /api/admin/** 仅允许管理员（role=1）访问；
 * 4. 处理器方法/类上标注 {@link RequireLogin} / {@link RequireAdmin} 时再校验登录态与角色；
 * 5. 白名单内请求若携带有效 JWT 依然注入用户信息（匿名可浏览、登录有身份）。
 * <p>
 * 拦截响应统一为 HTTP 200 + {@code {"code":401|403,...}}，由前端按 code 提示。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

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
            "/api/article/articles/*/watch"
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

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        String token = null;
        String auth = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (auth != null && auth.startsWith(SecurityConstants.BEARER_PREFIX)) {
            token = auth.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }
        Claims claims = token == null || token.isEmpty() ? null : jwtUtil.parse(token);

        Long uid = null;
        int role = SecurityConstants.ROLE_USER;
        if (claims != null) {
            uid = claims.get(SecurityConstants.CLAIM_USER_ID, Long.class);
            Integer claimRole = claims.get(SecurityConstants.CLAIM_ROLE, Integer.class);
            role = claimRole == null ? SecurityConstants.ROLE_USER : claimRole;
        }
        boolean admin = claims != null && role == SecurityConstants.ROLE_ADMIN;

        // 路径级：非白名单必须登录；/api/admin/** 必须管理员
        if (!isPublic(path, method) && claims == null) {
            return reject(response, 401, "请先登录后再操作");
        }
        if (matcher.match("/api/admin/**", path) && !admin) {
            return reject(response, 403, "需要管理员权限");
        }

        // 注解级：@RequireLogin / @RequireAdmin（方法优先，其次类）
        if (handler instanceof HandlerMethod handlerMethod) {
            boolean requireAdmin = hasAnnotation(handlerMethod, RequireAdmin.class);
            boolean requireLogin = requireAdmin || hasAnnotation(handlerMethod, RequireLogin.class);
            if (requireLogin && uid == null) {
                return reject(response, 401, "请先登录后再操作");
            }
            if (requireAdmin && !admin) {
                return reject(response, 403, "需要管理员权限");
            }
        }

        // 所有校验通过后才填充上下文（拦截返回 false 时 afterCompletion 不会执行，避免 ThreadLocal 残留）
        UserContext.set(new LoginUser(uid, role, ClientIpUtil.resolve(request)));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends java.lang.annotation.Annotation> type) {
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), type)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), type);
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

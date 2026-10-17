package cn.miskk.blog.common.auth;

import cn.hutool.json.JSONUtil;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.api.ResultCode;
import cn.miskk.blog.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * 认证拦截器：还原网关注入的用户信息到 UserContext，并校验 @RequireLogin / @RequireAdmin。
 * <p>
 * 服务仅信任来自网关的请求头（生产环境应通过内网/网络策略保证下游服务不直接对外暴露）。
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String userInfo = request.getHeader(SecurityConstants.HEADER_USER_INFO);
        if (userInfo != null && !userInfo.isBlank()) {
            try {
                LoginUser user = JSONUtil.toBean(userInfo, LoginUser.class);
                if (user.getRole() == null) {
                    user.setRole(SecurityConstants.ROLE_USER);
                }
                UserContext.set(user);
            } catch (Exception e) {
                log.warn("非法的用户上下文请求头 X-User-Info: {}", userInfo);
            }
        }

        Method method = handlerMethod.getMethod();
        Class<?> beanType = handlerMethod.getBeanType();
        boolean requireLogin = AnnotatedElementUtils.hasAnnotation(method, RequireLogin.class)
                || AnnotatedElementUtils.hasAnnotation(beanType, RequireLogin.class);
        boolean requireAdmin = AnnotatedElementUtils.hasAnnotation(method, RequireAdmin.class)
                || AnnotatedElementUtils.hasAnnotation(beanType, RequireAdmin.class);

        LoginUser user = UserContext.get();
        if ((requireLogin || requireAdmin) && user == null) {
            writeJson(response, Result.fail(ResultCode.UNAUTHORIZED));
            return false;
        }
        if (requireAdmin && (user.getRole() == null || user.getRole() != SecurityConstants.ROLE_ADMIN)) {
            writeJson(response, Result.fail(ResultCode.FORBIDDEN));
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private void writeJson(HttpServletResponse response, Result<?> result) throws Exception {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}

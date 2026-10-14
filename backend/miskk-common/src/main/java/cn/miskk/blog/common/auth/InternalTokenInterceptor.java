package cn.miskk.blog.common.auth;

import cn.hutool.json.JSONUtil;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.api.ResultCode;
import cn.miskk.blog.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 服务间内部接口拦截器：校验 X-Internal-Token，防止内部统计接口被外部调用。
 */
public class InternalTokenInterceptor implements HandlerInterceptor {

    private final String internalToken;

    public InternalTokenInterceptor(@Value("${miskk.internal-token:miskk-internal-token-dev}") String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader(SecurityConstants.HEADER_INTERNAL_TOKEN);
        if (internalToken.equals(token)) {
            return true;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSONUtil.toJsonStr(Result.fail(ResultCode.FORBIDDEN)));
        return false;
    }
}

package cn.miskk.blog.gateway.tools;

import cn.miskk.blog.common.auth.InternalTokenInterceptor;
import cn.miskk.blog.common.constant.SecurityConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内部令牌校验测试：/internal/** 只接受携带正确 X-Internal-Token 的服务间调用。
 */
class InternalTokenTest {

    private static final String TOKEN = "unit-test-internal-token";

    private final InternalTokenInterceptor interceptor = new InternalTokenInterceptor(TOKEN);

    @Test
    @DisplayName("携带正确 X-Internal-Token 的请求放行")
    void validTokenPasses() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityConstants.HEADER_INTERNAL_TOKEN, TOKEN);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    @DisplayName("错误的 token 返回 403 JSON 并拦截")
    void wrongTokenRejected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityConstants.HEADER_INTERNAL_TOKEN, "wrong-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertTrue(response.getContentAsString().contains("\"code\":403"), "应返回 403 结果 JSON");
    }

    @Test
    @DisplayName("缺失 token 返回 403 JSON 并拦截")
    void missingTokenRejected() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(new MockHttpServletRequest(), response, new Object()));
        assertTrue(response.getContentAsString().contains("\"code\":403"), "应返回 403 结果 JSON");
    }
}

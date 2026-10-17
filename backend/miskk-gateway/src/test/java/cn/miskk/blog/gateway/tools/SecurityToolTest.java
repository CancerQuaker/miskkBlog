package cn.miskk.blog.gateway.tools;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONUtil;
import cn.miskk.blog.common.auth.InternalTokenInterceptor;
import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全工具型单元测试（取代原 tools/GenBcryptHash 本地工具类）：
 * <ul>
 *   <li>密码哈希：生成 BCrypt 哈希（用于 sql/01_miskk_user.sql 管理员密码种子）；</li>
 *   <li>内部 token：/internal/** 的 X-Internal-Token 放行与拒绝；</li>
 *   <li>登录 token：网关签发的 JWT 解析验签，及 X-User-Info 上下文 JSON 往返。</li>
 * </ul>
 * <p>
 * 运行（在仓库根目录）：
 * <pre>mvn -pl backend/miskk-gateway test -Dtest=SecurityToolTest</pre>
 * 生成密码哈希可用 {@code -Dpwd=新密码} 指定密码（默认 admin123），
 * 控制台输出的哈希直接替换 SQL 种子数据中的旧值。
 */
class SecurityToolTest {

    /** 长度 ≥32 字节的测试密钥（JwtUtil 构造器强校验） */
    private static final String JWT_SECRET = "unit-test-jwt-secret-key-at-least-32-bytes";

    private static final String INTERNAL_TOKEN = "unit-test-internal-token";

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== 密码哈希 ====================

    @Test
    @DisplayName("生成 BCrypt 密码哈希并通过正误校验")
    void generatePasswordHash() {
        String password = System.getProperty("pwd", "admin123");
        String hash = BCrypt.hashpw(password);

        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希: " + hash);

        assertTrue(BCrypt.checkpw(password, hash), "正确密码应校验通过");
        assertFalse(BCrypt.checkpw(password + "x", hash), "错误密码应校验失败");
    }

    // ==================== 内部 token ====================

    @Test
    @DisplayName("携带正确 X-Internal-Token 的请求放行")
    void internalTokenPasses() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityConstants.HEADER_INTERNAL_TOKEN, INTERNAL_TOKEN);

        assertTrue(new InternalTokenInterceptor(INTERNAL_TOKEN)
                .preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    @DisplayName("错误或缺失的 token 返回 403 JSON 并拦截")
    void internalTokenRejected() throws Exception {
        InternalTokenInterceptor interceptor = new InternalTokenInterceptor(INTERNAL_TOKEN);

        MockHttpServletRequest wrongRequest = new MockHttpServletRequest();
        wrongRequest.addHeader(SecurityConstants.HEADER_INTERNAL_TOKEN, "wrong-token");
        MockHttpServletResponse wrongResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(wrongRequest, wrongResponse, new Object()));
        assertTrue(wrongResponse.getContentAsString().contains("\"code\":403"), "应返回 403 结果 JSON");

        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(new MockHttpServletRequest(), missingResponse, new Object()));
        assertTrue(missingResponse.getContentAsString().contains("\"code\":403"), "应返回 403 结果 JSON");
    }

    // ==================== 登录 token ====================

    @Test
    @DisplayName("网关签发的登录 token 可解析出 uid/role")
    void loginTokenSignAndParse() {
        JwtUtil jwtUtil = new JwtUtil(JWT_SECRET, 72);
        String token = jwtUtil.createToken(1L, SecurityConstants.ROLE_ADMIN);

        Claims claims = jwtUtil.parse(token);
        assertNotNull(claims, "合法 token 应解析成功");
        assertEquals(1L, claims.get(SecurityConstants.CLAIM_USER_ID, Long.class).longValue());
        assertEquals(SecurityConstants.ROLE_ADMIN, claims.get(SecurityConstants.CLAIM_ROLE, Integer.class).intValue());
    }

    @Test
    @DisplayName("篡改/乱码/过期的 token 解析失败返回 null")
    void loginTokenInvalidReturnsNull() {
        JwtUtil jwtUtil = new JwtUtil(JWT_SECRET, 72);
        String token = jwtUtil.createToken(1L, SecurityConstants.ROLE_USER);

        // 篡改签名尾部
        assertNull(jwtUtil.parse(token.substring(0, token.length() - 3) + "xxx"));
        assertNull(jwtUtil.parse("not-a-jwt"));
        assertNull(jwtUtil.parse(null));

        // 负有效期 → 签出来即过期
        JwtUtil expired = new JwtUtil(JWT_SECRET, -1);
        assertNull(expired.parse(expired.createToken(1L, SecurityConstants.ROLE_USER)));
    }

    @Test
    @DisplayName("登录人上下文经 X-User-Info JSON 往返还原（含匿名与未设置）")
    void userInfoJsonRoundTrip() {
        // 登录用户：网关 set 后序列化，下游 toBean 还原
        UserContext.set(new LoginUser(42L, SecurityConstants.ROLE_ADMIN, "127.0.0.1"));
        LoginUser restored = JSONUtil.toBean(UserContext.toJson(), LoginUser.class);
        assertEquals(42L, restored.getUserId().longValue());
        assertEquals(SecurityConstants.ROLE_ADMIN, restored.getRole().intValue());
        assertEquals("127.0.0.1", restored.getIp());

        // 匿名请求：userId 为 null 也能往返，ip 保留
        UserContext.set(new LoginUser(null, null, "192.168.1.1"));
        LoginUser anonymous = JSONUtil.toBean(UserContext.toJson(), LoginUser.class);
        assertNull(anonymous.getUserId());
        assertNotNull(anonymous.getIp());

        // 未设置上下文：toJson 为 null（Feign 将省略 X-User-Info 头）
        UserContext.clear();
        assertNull(UserContext.toJson());
    }
}

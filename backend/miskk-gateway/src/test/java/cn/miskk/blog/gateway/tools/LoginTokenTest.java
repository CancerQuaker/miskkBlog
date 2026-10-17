package cn.miskk.blog.gateway.tools;

import cn.hutool.json.JSONUtil;
import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 登录人 token 测试：网关签发的 JWT 可正确解析验签，
 * 且登录人上下文能经 X-User-Info（LoginUser JSON）往返还原。
 */
class LoginTokenTest {

    /** 长度 ≥32 字节的测试密钥（构造器强校验） */
    private static final String SECRET = "unit-test-jwt-secret-key-at-least-32-bytes";

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("网关签发的登录 token 可解析出 uid/role")
    void signAndParse() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 72);
        String token = jwtUtil.createToken(1L, SecurityConstants.ROLE_ADMIN);

        Claims claims = jwtUtil.parse(token);
        assertNotNull(claims, "合法 token 应解析成功");
        assertEquals(1L, claims.get(SecurityConstants.CLAIM_USER_ID, Long.class).longValue());
        assertEquals(SecurityConstants.ROLE_ADMIN, claims.get(SecurityConstants.CLAIM_ROLE, Integer.class).intValue());
    }

    @Test
    @DisplayName("篡改/乱码/过期的 token 解析失败返回 null")
    void invalidTokenReturnsNull() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 72);
        String token = jwtUtil.createToken(1L, SecurityConstants.ROLE_USER);

        // 篡改签名尾部
        assertNull(jwtUtil.parse(token.substring(0, token.length() - 3) + "xxx"));
        assertNull(jwtUtil.parse("not-a-jwt"));
        assertNull(jwtUtil.parse(null));

        // 负有效期 → 签出来即过期
        JwtUtil expired = new JwtUtil(SECRET, -1);
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

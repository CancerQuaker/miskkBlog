package cn.miskk.blog.tools;

import cn.hutool.crypto.digest.BCrypt;
import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全工具型单元测试：
 * <ul>
 *   <li>密码哈希：生成 BCrypt 哈希（用于 sql/01_miskk_user.sql 管理员密码种子）；</li>
 *   <li>登录 token：JWT 签发与解析验签。</li>
 * </ul>
 * <p>
 * 运行（在仓库根目录）：
 * <pre>mvn -pl backend/miskk-server test -Dtest=SecurityToolTest</pre>
 * 生成密码哈希：<pre>mvn -pl backend/miskk-server test -Dtest=SecurityToolTest#generatePasswordHash</pre>
 * 控制台输出的哈希直接替换 SQL 种子数据中的旧值。
 */
class SecurityToolTest {

    /** 长度 ≥32 字节的测试密钥（JwtUtil 构造器强校验） */
    private static final String JWT_SECRET = "unit-test-jwt-secret-key-at-least-32-bytes";

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== 密码哈希 ====================

    @Test
    @DisplayName("生成 BCrypt 密码哈希并通过正误校验")
    void generatePasswordHash() {
        String password = "admin123";
        String hash = BCrypt.hashpw(password);

        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希: " + hash);

        assertTrue(BCrypt.checkpw(password, hash), "正确密码应校验通过");
        assertFalse(BCrypt.checkpw(password + "x", hash), "错误密码应校验失败");
    }

    // ==================== 登录 token ====================

    @Test
    @DisplayName("签发的登录 token 可解析出 uid/role")
    void loginTokenSignAndParse() {
        JwtUtil jwtUtil = new JwtUtil(JWT_SECRET, 72);
        String token = jwtUtil.createToken(1L, SecurityConstants.ROLE_ADMIN);

        Claims claims = jwtUtil.parse(token);
        assertNotNull(claims, "合法 token 应解析成功");
        assertEquals(1L, claims.get(SecurityConstants.CLAIM_USER_ID, Long.class).longValue());
        assertEquals(SecurityConstants.ROLE_ADMIN, claims.get(SecurityConstants.CLAIM_ROLE, Integer.class).intValue());
    }

    @Test
    @DisplayName("篡改或密钥不匹配的 token 解析返回 null")
    void loginTokenRejected() {
        JwtUtil jwtUtil = new JwtUtil(JWT_SECRET, 72);
        String token = jwtUtil.createToken(1L, SecurityConstants.ROLE_USER);

        assertNull(jwtUtil.parse(token + "x"), "篡改后的 token 应解析失败");
        assertNull(new JwtUtil("another-unit-test-secret-key-at-least-32-bytes", 72).parse(token),
                "其他密钥签发的 token 应解析失败");
    }
}

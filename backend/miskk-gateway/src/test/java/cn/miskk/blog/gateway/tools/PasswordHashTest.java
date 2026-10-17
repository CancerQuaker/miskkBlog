package cn.miskk.blog.gateway.tools;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 生成 BCrypt 密码哈希（用于更新 sql/01_miskk_user.sql 中的管理员密码，
 * 取代原 tools/GenBcryptHash 本地工具类）。
 * <p>
 * 运行（在仓库根目录）：
 * <pre>mvn -pl backend/miskk-gateway test -Dtest=PasswordHashTest</pre>
 * 可用 {@code -Dpwd=新密码} 指定密码（默认 admin123），
 * 控制台输出的哈希直接替换 SQL 种子数据中的旧值。
 */
class PasswordHashTest {

    @Test
    @DisplayName("生成 BCrypt 密码哈希并通过正误校验")
    void generateAndVerify() {
        String password = System.getProperty("pwd", "admin123");
        String hash = BCrypt.hashpw(password);

        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希: " + hash);

        assertTrue(BCrypt.checkpw(password, hash), "正确密码应校验通过");
        assertFalse(BCrypt.checkpw(password + "x", hash), "错误密码应校验失败");
    }
}

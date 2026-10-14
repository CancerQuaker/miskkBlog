package cn.miskk.blog.common.auth;

import cn.miskk.blog.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 工具（HS256）。网关负责签发与校验，下游服务信任网关注入的请求头。
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final Duration expire;

    public JwtUtil(@Value("${miskk.jwt.secret:miskk-blog-jwt-secret-key-please-change-me-in-production-0001}") String secret,
                   @Value("${miskk.jwt.expire-hours:72}") long expireHours) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("miskk.jwt.secret 长度至少 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expire = Duration.ofHours(expireHours);
    }

    public String createToken(Long userId, Integer role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expire.toMillis()))
                .signWith(key)
                .compact();
    }

    /**
     * 校验并解析 token，失败返回 null。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public Duration getExpire() {
        return expire;
    }
}

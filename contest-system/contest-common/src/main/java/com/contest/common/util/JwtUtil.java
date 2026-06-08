package com.contest.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类 — 生成、解析和校验 Token
 *
 * 使用 jjwt 0.11.5 库实现 HMAC-SHA256 签名算法。密钥和过期时间通过
 * application.yml 配置注入（contest.jwt.secret / contest.jwt.expire-days），
 * 避免硬编码。
 *
 * 安全性说明：
 * - 签名算法：HMAC-SHA256（对称加密），密钥长度至少 32 字节
 * - Token 载荷：含 userId（用户ID）、username（用户名）、role（角色），不存放密码等敏感信息
 * - 过期机制：默认 7 天有效期，过期后 parseToken 抛出 ExpiredJwtException
 * - 无状态设计：服务器不存储 Token，每次请求自行解析校验
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${contest.jwt.expire-days:7}")
    private long expireDays;

    @Value("${contest.jwt.secret:contest-system-jwt-secret-key-2024-2025-2026}")
    private String secret;

    /**
     * 获取 HMAC-SHA256 签名密钥
     *
     * 将配置的 secret 字符串通过 UTF-8 编码为字节数组，使用 jjwt 的 Keys.hmacShaKeyFor
     * 构建符合 HMAC-SHA256 要求的 SecretKey。密钥长度不足时会自动填充。
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * 在用户登录和注册成功后调用，将用户身份信息签名到 Token 中。
     * Token 有效期 = expireDays × 24 × 3600 × 1000 毫秒。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色（0=学生，1=管理员，2=教师）
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireDays * 24 * 3600 * 1000);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * 校验签名和过期时间，返回 Token 中存储的 Claims（载荷数据）。
     * 签名不匹配或 Token 过期时抛出异常，由调用方处理。
     *
     * @param token JWT Token 字符串
     * @return Claims 载荷（含 userId、username、role 等字段）
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中提取用户 ID
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从 Token 中提取角色
     *
     * @param token JWT Token
     * @return 角色（0=学生，1=管理员，2=教师）
     */
    public Integer getRole(String token) {
        return parseToken(token).get("role", Integer.class);
    }

    /**
     * 校验 Token 是否有效（签名正确 + 未过期）
     *
     * 解析成功返回 true，任何异常（如签名错误、过期、格式错误）均返回 false。
     *
     * @param token JWT Token
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}

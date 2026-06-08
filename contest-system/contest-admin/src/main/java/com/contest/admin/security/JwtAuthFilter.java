package com.contest.admin.security;

import com.contest.common.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 身份认证过滤器
 *
 * 继承 OncePerRequestFilter 确保每次请求仅执行一次过滤。从请求头 Authorization: Bearer xxx
 * 中提取 JWT Token，解析出用户 ID、角色和权限，装配到 Spring Security 的 SecurityContext 中，
 * 后续请求即可通过 SecurityUtil.getCurrentUserId() 获取当前用户信息。
 *
 * 安全性说明：
 * - Token 使用 HMAC-SHA256 签名（jjwt 库），防止伪造
 * - Token 过期时间由配置文件控制（默认 7 天），过期后自动失效
 * - 解析失败时仅记录 warn 日志，不阻断请求（由 SecurityConfig 的 .anyRequest().authenticated() 兜底返回 401）
 * - 不携带 Token 或格式错误时直接放行（未认证用户只能访问公开接口）
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    public JwtAuthFilter(JwtUtil jwtUtil, PermissionService permissionService) {
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
    }

    /**
     * 核心过滤逻辑：从请求头解析 Token → 提取用户身份 → 查询权限 → 装配到 SecurityContext
     *
     * 执行流程：
     * 1. 从 Authorization 头提取 Bearer Token
     * 2. 调用 JwtUtil 解析 Token，获取 userId / role / username
     * 3. 调用 PermissionService 获取该用户的权限编码集合（含角色权限 + 个人额外权限）
     * 4. 构造 UsernamePasswordAuthenticationToken，设置 principal=userId, authorities=权限列表
     * 5. 写入 SecurityContextHolder，后续请求通过 SecurityContext 获取当前用户
     *
     * 性能说明：Token 解析和权限查询在每次请求时都会执行，但均基于内存操作或简单 DB 查询，
     * 单次过滤耗时通常在 10ms 以内，不影响页面加载 &lt;2s 的目标。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserId(token);
            Integer role = jwtUtil.getRole(token);
            String username = jwtUtil.parseToken(token).get("username", String.class);
            if (userId == null || role == null) {
                filterChain.doFilter(request, response);
                return;
            }
            Set<String> permissions = permissionService.getPermissions(userId, role);
            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            String roleName = role == 1 ? "ADMIN" : role == 2 ? "TEACHER" : "STUDENT";
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);
            auth.setDetails(username);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (SignatureException e) {
            log.warn("JWT signature verification failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (IllegalArgumentException e) {
            log.warn("JWT token argument invalid: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}

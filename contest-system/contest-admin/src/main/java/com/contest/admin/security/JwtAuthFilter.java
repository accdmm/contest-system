package com.contest.admin.security;

import com.contest.common.util.JwtUtil;
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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    public JwtAuthFilter(JwtUtil jwtUtil, PermissionService permissionService) {
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
    }

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
        } catch (Exception e) {
            log.warn("JWT auth failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}

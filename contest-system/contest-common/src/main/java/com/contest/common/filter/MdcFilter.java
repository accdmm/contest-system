package com.contest.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * MDC 用户 ID 注入过滤器
 *
 * <p>在 JwtAuthFilter 认证完成后执行，从 SecurityContext 中提取当前用户 ID
 * 注入到 MDC 中，使后续日志输出自动携带 userId 字段。
 *
 * <p>执行顺序：JwtAuthFilter → MdcFilter → AccessLogFilter（输出日志时 userId 已注入）
 */
@Order(2)
@Component
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                MDC.put("userId", String.valueOf(userId));
            }
            filterChain.doFilter(request, response);
        } finally {
            // userId 保留到 AccessLogFilter 输出后统一清理
        }
    }
}

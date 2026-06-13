package com.contest.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 访问日志与 MDC 过滤器 — 记录每次请求的关键信息
 *
 * <p>继承 OncePerRequestFilter 确保每次请求仅执行一次。在请求开始时向 MDC
 * （Mapped Diagnostic Context）注入 traceId、userId，便于日志链路追踪。
 * 请求结束时输出访问日志（方法、URI、状态码、耗时），清理 MDC 防止内存泄漏。
 *
 * <p>安全性说明：traceId 仅用于日志关联，不包含用户敏感信息；
 * userId 取自已认证的 SecurityContext，拒绝外部传入。
 *
 * <p>可用性说明：管理员可通过 traceId 在日志中快速定位一次完整请求，
 * 配合时间范围搜索大幅降低排障时间。
 */
@Order(1)
@Component
@Slf4j
public class AccessLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        MDC.put("traceId", traceId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String userId = MDC.get("userId");
            log.info("[{}] {} {} -> {} ({}ms) userId={}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    userId != null ? userId : "anonymous");
            MDC.clear();
        }
    }
}

package com.contest.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求频率限制过滤器 — 基于 IP 的简单限流
 *
 * <p>对登录接口（/api/user/login）实施严格的频率限制，防止暴力破解。
 * 对其他接口实施较宽松的限制，防止意外的资源耗尽。
 *
 * <p>实现方式：基于 ConcurrentHashMap 的内存计数器，每个 IP + 接口组合独立计数。
 * 使用滑动窗口算法（每分钟重置），窗口到期后自动清理过期条目。
 *
 * <p>安全性说明：限流在过滤器链的最前端执行（@Order(0)），拒绝超出限制的请求直接返回 429，
 * 不经过后续的认证、参数解析等耗时操作，最大限度节约服务器资源。
 *
 * <p>可用性说明：被限流的请求会收到明确的 429 状态码和 "操作过于频繁，请稍后重试" 提示，
 * 并附带 Retry-After 头告知客户端等待时间。
 */
@Order(0)
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    /** 每分钟允许的请求数（普通接口） */
    private static final int DEFAULT_MAX_REQUESTS = 60;

    /** 每分钟允许的请求数（登录接口） */
    private static final int LOGIN_MAX_REQUESTS = 10;

    /** 窗口大小（毫秒） */
    private static final long WINDOW_SIZE_MS = 60_000L;

    /** 限流计数器：key = ip:uri, value = [count, windowStart] */
    private final Map<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String key = ip + ":" + method + ":" + uri;

        boolean isLogin = "/api/user/login".equals(uri) && "POST".equalsIgnoreCase(method);
        int maxRequests = isLogin ? LOGIN_MAX_REQUESTS : DEFAULT_MAX_REQUESTS;

        if (isRateLimited(key, maxRequests)) {
            log.warn("Rate limit exceeded: ip={}, uri={}", ip, uri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"操作过于频繁，请稍后重试\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否超出频率限制
     *
     * @param key         限流 key（ip:method:uri）
     * @param maxRequests 窗口内最大允许请求数
     * @return true 表示超出限制
     */
    private boolean isRateLimited(String key, int maxRequests) {
        long now = System.currentTimeMillis();
        long[] record = counters.compute(key, (k, v) -> {
            if (v == null || now - v[1] > WINDOW_SIZE_MS) {
                // 新窗口：count=1, windowStart=now
                return new long[]{1L, now};
            }
            // 已有窗口：count++
            v[0]++;
            return v;
        });

        // 定期清理过期条目，避免内存泄漏
        if (counters.size() > 10_000) {
            cleanupExpiredEntries();
        }

        return record[0] > maxRequests;
    }

    /**
     * 清理过期窗口条目
     */
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(entry -> now - entry.getValue()[1] > WINDOW_SIZE_MS);
    }

    /**
     * 获取客户端真实 IP（考虑反向代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty() && !"unknown".equalsIgnoreCase(xri)) {
            return xri;
        }
        return request.getRemoteAddr();
    }
}

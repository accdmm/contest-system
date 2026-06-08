package com.contest.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 — 通过 ThreadLocal 获取当前登录用户上下文
 *
 * <p>基于 Spring Security 的 SecurityContextHolder 实现，其默认策略 MODE_THREADLOCAL
 * 将 SecurityContext 绑定到当前请求线程中。在 JwtAuthFilter 完成 Token 解析和认证后，
 * 将用户信息写入 SecurityContextHolder，此处即可读取。
 *
 * <p>数据流：JwtAuthFilter（Filter 层写入）→ SecurityContextHolder（ThreadLocal 存储）
 * → Controller/Service（此处读取）→ 请求结束自动清除（由 Spring Security 的
 * SecurityContextHolderFilter 在请求完成后调用 clearContext()）。
 *
 * <p>安全性说明：ThreadLocal 存储天然线程隔离，不同请求互不干扰，无需加锁。
 * 请求结束后自动清理，避免内存泄漏。
 *
 * <p>可用性说明：未登录或匿名用户调用返回 null，调用方需自行判空。
 * 业务代码无需手动传递 userId，统一通过此工具类获取，降低参数传递复杂度。
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户 ID
     *
     * <p>从 SecurityContextHolder 中提取 Authentication.getPrincipal()。
     * Principal 在 JwtAuthFilter 中被设置为 userId（Long 类型）。
     *
     * @return 用户 ID，未登录或匿名访问返回 null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }
}

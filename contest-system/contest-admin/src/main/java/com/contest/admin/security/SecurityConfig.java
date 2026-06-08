package com.contest.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 *
 * 采用无状态（STATELESS）会话模式，完全依赖 JWT Token 进行身份认证。
 * 前后端分离架构下不使用 Session 和 Cookie，避免 CSRF 攻击面。
 *
 * 安全性说明：
 * - CSRF 防护关闭：前后端分离架构通过 Token 认证，无需 CSRF Token
 * - 无状态会话：不创建 HttpSession，每次请求独立鉴权
 * - URL 权限白名单：登录、注册、学院/专业列表、首页展示等公开接口无需认证
 * - 其余所有请求必须通过 JWT 认证（.anyRequest().authenticated()）
 * - 方法级权限：通过 @EnableMethodSecurity 启用 @PreAuthorize 注解进行精细权限控制
 *
 * 性能说明：无状态会话避免了 Session 存储和查询开销，配合 JWT 的自包含特性，
 * 每次请求只需一次 Token 解析即可完成认证，适合高并发场景。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * 配置安全过滤链
     *
     * 规则说明：
     * - POST /api/user/login + /api/user/register — 未登录可访问
     * - GET /api/colleges, /api/majors — 前端下拉框获取数据
     * - GET /api/contest/{id}, /api/contest/page, /hot, /latest — 首页和搜索页公开访问
     * - GET /api/cms/** — 轮播图、公告公开访问
     * - GET /api/uploads/** — 上传资源（图片、附件）公开访问
     * - 其他所有请求需要认证
     *
     * JwtAuthFilter 在 UsernamePasswordAuthenticationFilter 之前执行，确保 Token 认证优先。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/user/login", "/api/user/register",
                    "/api/user/colleges", "/api/user/majors").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contest/{id:[0-9]+}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contest/page", "/api/contest/hot", "/api/contest/latest").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cms/banners", "/api/cms/announcements", "/api/cms/{id:[0-9]+}").permitAll()
                .requestMatchers("/api/uploads/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

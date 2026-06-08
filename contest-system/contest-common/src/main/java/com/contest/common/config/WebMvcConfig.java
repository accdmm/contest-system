package com.contest.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 统一处理跨域请求
 *
 * <p>前后端分离架构下，前端开发服务器（:3000）与后端（:8080）不同源，
 * 需配置跨域资源共享（CORS）。生产部署时通过 contest.cors.allowed-origins
 * 配置项限定允许的来源域名，开发环境可使用 "*" 允许所有来源。
 *
 * <p>安全性说明：
 * <ul>
 *   <li>生产环境通过 application-prod.yml 的 contest.cors.allowed-origins 限定具体域名</li>
 *   <li>allowCredentials=true — 允许携带 Cookie/Authorization 头</li>
 *   <li>maxAge=3600 — 预检请求结果缓存 1 小时，减少 OPTIONS 请求次数</li>
 * </ul>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${contest.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

package com.contest.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 统一处理跨域请求
 *
 * 前后端分离架构下，前端开发服务器（:3000）与后端（:8080）不同源，
 * 需配置跨域资源共享（CORS）。生产部署时同域下则无此需求。
 *
 * 安全性说明：
 * - allowedOriginPatterns="*" — 开发环境允许所有来源，生产环境应限定具体域名
 * - allowCredentials=true — 允许携带 Cookie/Authorization 头
 * - maxAge=3600 — 预检请求结果缓存 1 小时，减少 OPTIONS 请求次数
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

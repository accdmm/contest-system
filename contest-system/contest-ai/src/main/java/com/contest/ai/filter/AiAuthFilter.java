package com.contest.ai.filter;

import com.contest.common.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class AiAuthFilter implements Filter {

    private final JwtUtil jwtUtil;

    public AiAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        if (!path.startsWith("/api/ai/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(res, "未登录");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                writeJson(res, "token无效或已过期");
                return;
            }
            req.setAttribute("userId", userId);
            chain.doFilter(request, response);
        } catch (Exception e) {
            writeJson(res, "token无效或已过期");
        }
    }

    private void writeJson(HttpServletResponse res, String message) throws IOException {
        res.setStatus(401);
        res.setContentType("application/json;charset=utf-8");
        res.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}

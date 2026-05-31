package com.contest.ai.controller;

import com.contest.ai.entity.ChatEventVO;
import com.contest.ai.entity.ChatRequest;
import com.contest.ai.service.AiChatService;
import com.contest.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final JwtUtil jwtUtil;

    public AiChatController(AiChatService aiChatService, JwtUtil jwtUtil) {
        this.aiChatService = aiChatService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public Flux<ChatEventVO> chat(@RequestBody ChatRequest request, HttpServletRequest servletRequest) {
        Long userId = resolveUserId(servletRequest);
        if (userId == null) {
            return Flux.just(ChatEventVO.error("未登录或登录已过期"));
        }
        return aiChatService.chat(request, userId);
    }

    @PostMapping("/stop/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public void stop(@PathVariable Long sessionId) {
        aiChatService.stop(sessionId);
    }

    private Long resolveUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            try {
                return jwtUtil.getUserId(authHeader.substring(7));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

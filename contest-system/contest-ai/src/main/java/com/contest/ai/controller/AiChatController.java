package com.contest.ai.controller;

import com.contest.ai.entity.ChatEventVO;
import com.contest.ai.entity.ChatRequest;
import com.contest.ai.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public Flux<ChatEventVO> chat(@RequestBody ChatRequest request, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        return aiChatService.chat(request, userId);
    }

    @PostMapping("/stop/{sessionId}")
    public void stop(@PathVariable Long sessionId) {
        aiChatService.stop(sessionId);
    }
}

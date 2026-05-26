package com.contest.ai.controller;

import com.contest.ai.entity.ChatRequest;
import com.contest.ai.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatRequest request, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        return aiChatService.chat(request, userId);
    }
}

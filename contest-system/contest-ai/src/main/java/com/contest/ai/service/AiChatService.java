package com.contest.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.contest.ai.entity.ChatRequest;

public interface AiChatService {

    SseEmitter chat(ChatRequest request, Long userId);
}

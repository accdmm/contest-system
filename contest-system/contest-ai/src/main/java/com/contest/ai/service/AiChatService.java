package com.contest.ai.service;

import com.contest.ai.entity.ChatEventVO;
import com.contest.ai.entity.ChatRequest;
import reactor.core.publisher.Flux;

public interface AiChatService {

    Flux<ChatEventVO> chat(ChatRequest request, Long userId);

    void stop(Long sessionId);
}

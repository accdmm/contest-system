package com.contest.ai.controller;

import com.contest.ai.entity.AiConversationDO;
import com.contest.ai.entity.AiMessageDO;
import com.contest.ai.dto.ChatEventVO;
import com.contest.ai.dto.ChatRequest;
import com.contest.ai.service.AiChatService;
import com.contest.common.dto.Result;
import com.contest.common.enums.ResultCodeEnum;
import com.contest.common.security.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public Flux<ChatEventVO> chat(@RequestBody ChatRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
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

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public Result<List<AiConversationDO>> listConversations() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return Result.success(aiChatService.listConversations(userId));
    }

    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public Result<List<AiMessageDO>> listMessages(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return Result.success(aiChatService.listMessages(id, userId));
    }

    @PostMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        aiChatService.deleteConversation(id, userId);
        return Result.success();
    }

    @PostMapping("/conversations/batch-delete")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteConversations(@RequestBody List<Long> ids) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        aiChatService.deleteConversations(ids, userId);
        return Result.success();
    }
}

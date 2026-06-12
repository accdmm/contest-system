package com.contest.ai.controller;

import com.contest.ai.entity.AiConversation;
import com.contest.ai.entity.AiMessage;
import com.contest.ai.dto.ChatEventDTO;
import com.contest.ai.dto.ChatRequestDTO;
import com.contest.ai.service.AiChatService;
import com.contest.common.result.Result;
import com.contest.common.result.ResultCodeEnum;
import com.contest.common.security.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/** AI智能咨询接口 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /** SSE流式对话接口 */
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public Flux<ChatEventDTO> chat(@RequestBody ChatRequestDTO request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Flux.just(ChatEventDTO.error("未登录或登录已过期"));
        }
        return aiChatService.chat(request, userId);
    }

    /** 停止AI生成 */
    @PostMapping("/stop/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public void stop(@PathVariable Long sessionId) {
        aiChatService.stop(sessionId);
    }

    /** 获取当前用户会话列表 */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public Result<List<AiConversation>> listConversations() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return Result.success(aiChatService.listConversations(userId));
    }

    /** 获取会话消息列表 */
    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public Result<List<AiMessage>> listMessages(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return Result.success(aiChatService.listMessages(id, userId));
    }

    /** 删除单个会话 */
    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        aiChatService.deleteConversation(id, userId);
        return Result.success();
    }

    /** 批量删除会话 */
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

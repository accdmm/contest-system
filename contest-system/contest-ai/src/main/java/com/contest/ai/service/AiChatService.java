package com.contest.ai.service;

import com.contest.ai.entity.AiConversationDO;
import com.contest.ai.entity.AiMessageDO;
import com.contest.ai.dto.ChatEventDTO;
import com.contest.ai.dto.ChatRequestDTO;
import reactor.core.publisher.Flux;

import java.util.List;

/** AI对话服务接口 */
public interface AiChatService {

    /** SSE流式对话：自动创建或继续会话，返回事件流
     * @param request 对话请求
     * @param userId 当前用户ID
     * @return SSE事件流 */
    Flux<ChatEventDTO> chat(ChatRequestDTO request, Long userId);

    /** 停止AI生成
     * @param sessionId 会话ID */
    void stop(Long sessionId);

    /** 获取当前用户的所有会话列表，按更新时间倒序
     * @param userId 用户ID
     * @return 会话列表 */
    List<AiConversationDO> listConversations(Long userId);

    /** 获取指定会话的消息列表
     * @param conversationId 会话ID
     * @param userId 用户ID（校验归属）
     * @return 消息列表 */
    List<AiMessageDO> listMessages(Long conversationId, Long userId);

    /** 删除会话及其所有消息（校验归属）
     * @param conversationId 会话ID
     * @param userId 用户ID */
    void deleteConversation(Long conversationId, Long userId);

    /** 批量删除会话（校验归属）
     * @param ids 会话ID列表
     * @param userId 用户ID */
    void deleteConversations(List<Long> ids, Long userId);
}

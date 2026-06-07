package com.contest.ai.service;

import com.contest.ai.entity.AiConversationDO;
import com.contest.ai.entity.AiMessageDO;
import com.contest.ai.dto.ChatEventVO;
import com.contest.ai.dto.ChatRequest;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatService {

    Flux<ChatEventVO> chat(ChatRequest request, Long userId);

    void stop(Long sessionId);

    /** 获取当前用户的所有会话列表，按更新时间倒序 */
    List<AiConversationDO> listConversations(Long userId);

    /** 获取指定会话的消息列表 */
    List<AiMessageDO> listMessages(Long conversationId, Long userId);

    /** 删除会话及其所有消息（校验归属） */
    void deleteConversation(Long conversationId, Long userId);

    /** 批量删除会话（校验归属） */
    void deleteConversations(List<Long> ids, Long userId);
}

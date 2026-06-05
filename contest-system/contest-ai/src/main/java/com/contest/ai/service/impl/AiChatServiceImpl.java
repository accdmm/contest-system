package com.contest.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.ai.config.AiProperties;
import com.contest.ai.entity.AiConversation;
import com.contest.ai.entity.AiMessage;
import com.contest.ai.entity.ChatEventVO;
import com.contest.ai.entity.ChatRequest;
import com.contest.ai.mapper.AiConversationMapper;
import com.contest.ai.mapper.AiMessageMapper;
import com.contest.ai.service.AiChatService;
import com.contest.ai.tool.ChatTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final ChatClient chatClient;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiProperties aiProperties;
    private final ChatTools chatTools;
    private final Map<Long, Boolean> generateStatus = new ConcurrentHashMap<>();

    public AiChatServiceImpl(ChatClient.Builder chatClientBuilder,
                             AiConversationMapper conversationMapper,
                             AiMessageMapper messageMapper,
                             AiProperties aiProperties,
                             ChatTools chatTools) {
        this.chatClient = chatClientBuilder.build();
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.aiProperties = aiProperties;
        this.chatTools = chatTools;
    }

    @Override
    public Flux<ChatEventVO> chat(ChatRequest request, Long userId) {
        AiConversation conversation = getOrCreateConversation(request, userId);
        Long conversationId = conversation.getId();

        saveUserMessage(conversationId, request.getMessage());

        List<Message> history = loadHistory(conversationId);

        ToolCallback[] wrappedCallbacks = buildWrappedToolCallbacks(userId);

        StringBuilder fullResponse = new StringBuilder();
        Long sessionId = conversationId;

        return Flux.concat(
                Flux.just(ChatEventVO.start(conversationId)),
                chatClient.prompt()
                    .system(spec -> spec.text(aiProperties.getSystemPrompt()))
                    .messages(history)
                    .toolCallbacks(wrappedCallbacks)
                    .user(request.getMessage())
                    .stream()
                    .content()
                    .doFirst(() -> generateStatus.put(sessionId, true))
                    .takeWhile(r -> generateStatus.getOrDefault(sessionId, false))
                    .filter(text -> text != null && !text.isEmpty())
                    .doOnNext(text -> fullResponse.append(text))
                    .map(ChatEventVO::data)
                .onErrorResume(e -> {
                    log.error("AI chat stream error, sessionId={}", sessionId, e);
                    String msg = e.getMessage();
                    if (msg != null && msg.length() > 80) msg = msg.substring(0, 80) + "...";
                    return Flux.just(ChatEventVO.error(msg != null ? msg : "服务器内部错误"));
                })
                .concatWithValues(ChatEventVO.stop())
                .doFinally(signalType -> {
                    generateStatus.remove(sessionId);
                    if (signalType == SignalType.ON_COMPLETE && fullResponse.length() > 0) {
                        saveAssistantMessage(conversationId, fullResponse.toString());
                        updateTitle(conversation, request.getMessage(), fullResponse.toString());
                    }
                })
        );
    }

    @Override
    public void stop(Long sessionId) {
        generateStatus.remove(sessionId);
    }

    private ToolCallback[] buildWrappedToolCallbacks(Long userId) {
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(chatTools)
                .build()
                .getToolCallbacks();
        ToolCallback[] wrapped = new ToolCallback[toolCallbacks.length];
        for (int i = 0; i < toolCallbacks.length; i++) {
            ToolCallback original = toolCallbacks[i];
            wrapped[i] = new ToolCallback() {
                @Override
                public ToolDefinition getToolDefinition() {
                    return original.getToolDefinition();
                }
                @Override
                public String call(String input) {
                    ChatTools.setCurrentUserId(userId);
                    try {
                        return original.call(input);
                    } finally {
                        ChatTools.clearUserId();
                    }
                }
            };
        }
        return wrapped;
    }

    private List<Message> loadHistory(Long conversationId) {
        List<AiMessage> records = messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreateTime)
        );
        return records.stream().map(msg -> {
            if ("user".equals(msg.getRole())) {
                return new UserMessage(msg.getContent());
            } else if ("assistant".equals(msg.getRole())) {
                return new AssistantMessage(msg.getContent());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private AiConversation getOrCreateConversation(ChatRequest request, Long userId) {
        if (request.getConversationId() != null) {
            AiConversation existing = conversationMapper.selectById(request.getConversationId());
            if (existing != null && existing.getUserId().equals(userId)) {
                existing.setUpdateTime(LocalDateTime.now());
                conversationMapper.updateById(existing);
                return existing;
            }
        }
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(null);
        conversationMapper.insert(conversation);
        return conversation;
    }

    private void saveUserMessage(Long conversationId, String content) {
        AiMessage msg = new AiMessage();
        msg.setConversationId(conversationId);
        msg.setRole("user");
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    private void saveAssistantMessage(Long conversationId, String content) {
        AiMessage msg = new AiMessage();
        msg.setConversationId(conversationId);
        msg.setRole("assistant");
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    private void updateTitle(AiConversation conversation, String question, String response) {
        if (conversation.getTitle() == null && response.length() > 0) {
            String title = question.length() > 30 ? question.substring(0, 30) + "..." : question;
            conversation.setTitle(title);
            conversationMapper.updateById(conversation);
        }
    }

    @Override
    public List<AiMessage> listMessages(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new com.contest.common.exception.BusinessException("会话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new com.contest.common.exception.BusinessException("无权查看其他用户的会话");
        }
        return messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreateTime));
    }

    @Override
    public List<AiConversation> listConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getUpdateTime));
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new com.contest.common.exception.BusinessException("会话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new com.contest.common.exception.BusinessException("无权删除其他用户的会话");
        }
        // 先删关联消息，再删会话（无级联外键）
        messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId));
        conversationMapper.deleteById(conversationId);
    }

    @Override
    public void deleteConversations(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) return;
        List<AiConversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .in(AiConversation::getId, ids)
                        .eq(AiConversation::getUserId, userId));
        if (list.isEmpty()) return;
        List<Long> validIds = list.stream().map(AiConversation::getId).collect(Collectors.toList());
        messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .in(AiMessage::getConversationId, validIds));
        conversationMapper.deleteByIds(validIds);
    }
}

package com.contest.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.ai.config.AiProperties;
import com.contest.ai.entity.AiConversation;
import com.contest.ai.entity.AiMessage;
import com.contest.ai.entity.ChatRequest;
import com.contest.ai.mapper.AiConversationMapper;
import com.contest.ai.mapper.AiMessageMapper;
import com.contest.ai.service.AiChatService;
import com.contest.ai.tool.ChatTools;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final StreamingChatModel streamingChatModel;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiProperties aiProperties;
    private final ChatTools chatTools;
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ai-chat-" + r.hashCode());
        t.setDaemon(true);
        return t;
    });

    public AiChatServiceImpl(StreamingChatModel streamingChatModel,
                             AiConversationMapper conversationMapper,
                             AiMessageMapper messageMapper,
                             AiProperties aiProperties,
                             ChatTools chatTools) {
        this.streamingChatModel = streamingChatModel;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.aiProperties = aiProperties;
        this.chatTools = chatTools;
    }

    @Override
    public SseEmitter chat(ChatRequest request, Long userId) {
        SseEmitter emitter = new SseEmitter(300000L);

        executor.execute(() -> {
            try {
                processStream(emitter, request, userId);
            } catch (Exception e) {
                log.error("AI chat stream failed", e);
                String msg = e.getMessage();
                if (msg != null && msg.length() > 80) msg = msg.substring(0, 80) + "...";
                try {
                    emitter.send(SseEmitter.event().name("error").data(msg != null ? msg : "服务器内部错误"));
                    emitter.complete();
                } catch (IOException ignored) {}
            }
        });

        return emitter;
    }

    @Transactional
    protected void processStream(SseEmitter emitter, ChatRequest request, Long userId) throws IOException {
        AiConversation conversation = getOrCreateConversation(request, userId);
        Long conversationId = conversation.getId();

        saveUserMessage(conversationId, request.getMessage());

        List<Message> messages = buildChatMessages(conversationId, request.getMessage());
        ChatTools.setCurrentUserId(userId);
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
            .toolObjects(chatTools)
            .build()
            .getToolCallbacks();
        ToolCallback[] wrappedCallbacks = wrapToolCallbacks(toolCallbacks, userId);
        ToolCallingChatOptions options = ToolCallingChatOptions.builder()
            .toolCallbacks(Arrays.asList(wrappedCallbacks))
            .build();
        Prompt prompt = new Prompt(messages, options);

        StringBuilder fullResponse = new StringBuilder();
        try {
            streamingChatModel.stream(prompt).toStream().forEach(chunk -> {
                String content = chunk.getResult().getOutput().getText();
                if (content != null && !content.isEmpty()) {
                    fullResponse.append(content);
                    try {
                        emitter.send(SseEmitter.event().name("message").data(content));
                    } catch (IOException e) {
                        throw new RuntimeException("SSE发送失败", e);
                    }
                }
            });

            saveAssistantMessage(conversationId, fullResponse.toString());

            if (conversation.getTitle() == null && fullResponse.length() > 0) {
                String title = request.getMessage().length() > 30
                    ? request.getMessage().substring(0, 30) + "..."
                    : request.getMessage();
                conversation.setTitle(title);
                conversationMapper.updateById(conversation);
            }

            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } finally {
            ChatTools.clearUserId();
        }
    }

    private static ToolCallback[] wrapToolCallbacks(ToolCallback[] originals, Long userId) {
        ToolCallback[] wrapped = new ToolCallback[originals.length];
        for (int i = 0; i < originals.length; i++) {
            ToolCallback original = originals[i];
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

    private List<Message> buildChatMessages(Long conversationId, String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(aiProperties.getSystemPrompt()));

        List<AiMessage> history = messageMapper.selectList(
            new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getCreateTime)
        );

        for (AiMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        return messages;
    }
}

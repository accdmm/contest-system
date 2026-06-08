package com.contest.ai.dto;

import java.io.Serializable;

/** AI对话请求DTO */
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID（为空则新建会话） */
    private Long conversationId;
    /** 用户消息内容 */
    private String message;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

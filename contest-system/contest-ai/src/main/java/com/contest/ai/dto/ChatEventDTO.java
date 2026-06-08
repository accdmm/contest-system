package com.contest.ai.dto;

import java.io.Serializable;
import com.contest.ai.enums.ChatEventTypeEnum;

/** SSE事件DTO */
public class ChatEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 事件类型（start/data/error/stop） */
    private String eventType;
    /** 事件数据 */
    private String eventData;

    public ChatEventDTO() {}

    public ChatEventDTO(String eventType, String eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public static ChatEventDTO start(Long conversationId) {
        return new ChatEventDTO(ChatEventTypeEnum.START.getValue(), String.valueOf(conversationId));
    }

    public static ChatEventDTO data(String text) {
        return new ChatEventDTO(ChatEventTypeEnum.DATA.getValue(), text);
    }

    public static ChatEventDTO error(String msg) {
        return new ChatEventDTO(ChatEventTypeEnum.ERROR.getValue(), msg);
    }

    public static ChatEventDTO stop() {
        return new ChatEventDTO(ChatEventTypeEnum.STOP.getValue(), null);
    }
}

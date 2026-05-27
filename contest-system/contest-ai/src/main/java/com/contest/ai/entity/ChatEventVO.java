package com.contest.ai.entity;

public class ChatEventVO {

    private String eventType;
    private String eventData;

    public ChatEventVO() {}

    public ChatEventVO(String eventType, String eventData) {
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

    public static ChatEventVO start(Long conversationId) {
        return new ChatEventVO(ChatEventTypeEnum.START.getValue(), String.valueOf(conversationId));
    }

    public static ChatEventVO data(String text) {
        return new ChatEventVO(ChatEventTypeEnum.DATA.getValue(), text);
    }

    public static ChatEventVO error(String msg) {
        return new ChatEventVO(ChatEventTypeEnum.ERROR.getValue(), msg);
    }

    public static ChatEventVO stop() {
        return new ChatEventVO(ChatEventTypeEnum.STOP.getValue(), null);
    }
}

package com.contest.ai.enums;

public enum ChatEventTypeEnum {

    START("start"),
    DATA("data"),
    ERROR("error"),
    STOP("stop");

    private final String value;

    ChatEventTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

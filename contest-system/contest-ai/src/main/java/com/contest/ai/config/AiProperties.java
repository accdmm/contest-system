package com.contest.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "contest.ai")
public class AiProperties {

    private String systemPrompt = "你是一个高校竞赛报名管理系统的AI助手，可以帮助用户查询竞赛信息、报名状态等。回答要简洁准确。";

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}

package com.contest.message.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 发送通知请求参数 */
public class NotificationSendParam {

    /** 接收用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 通知类型 */
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /** 通知标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 通知内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 关联业务类型 */
    private String relatedType;

    /** 关联业务ID */
    private Long relatedId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
}

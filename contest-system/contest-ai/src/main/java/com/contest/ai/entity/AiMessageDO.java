package com.contest.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** AI消息实体 */
@TableName("ai_message")
public class AiMessageDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话ID */
    private Long conversationId;

    /** 消息角色（user/assistant） */
    private String role;

    /** 消息内容 */
    @TableField("`content`")
    private String content;

    /** 消耗Token数 */
    private Integer tokens;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @JsonIgnore
    @TableLogic
    private Integer isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTokens() { return tokens; }
    public void setTokens(Integer tokens) { this.tokens = tokens; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

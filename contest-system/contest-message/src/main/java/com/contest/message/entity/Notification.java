package com.contest.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 通知实体 */
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID（0表示广播） */
    private Long userId;

    /** 通知类型 */
    private Integer type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联业务ID */
    private Long relatedId;

    /** 关联业务类型 */
    private String relatedType;

    /** 是否已读（0-未读，1-已读） */
    private Integer isRead;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @JsonIgnore
    @TableLogic
    private Integer isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

package com.contest.message.param;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 更新 CMS 内容请求参数 */
public class CmsContentUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "内容类型不能为空")
    private Integer contentType;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String imageUrl;

    private String linkUrl;

    private Integer sortOrder;

    private String position;

    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getContentType() { return contentType; }
    public void setContentType(Integer contentType) { this.contentType = contentType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

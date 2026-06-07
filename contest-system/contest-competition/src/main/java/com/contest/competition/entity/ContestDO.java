package com.contest.competition.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/** 竞赛实体 */
@TableName("contest")
public class ContestDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private String level;

    private String organizer;

    private LocalDateTime contestTime;

    private LocalDateTime registerStartTime;

    private LocalDateTime registerEndTime;

    private String location;

    private String coverImageUrl;

    private String description;

    private String attachmentUrls;

    private Integer contestType;

    private Integer teamMinSize;

    private Integer teamMaxSize;

    private Integer maxParticipants;

    private Long createBy;

    @TableField(exist = false)
    private String creatorName;

    private Integer status;

    private Integer currentCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return "ContestDO{id=" + id + ", name='" + name + "', status=" + status + "}";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public LocalDateTime getContestTime() { return contestTime; }
    public void setContestTime(LocalDateTime contestTime) { this.contestTime = contestTime; }
    public LocalDateTime getRegisterStartTime() { return registerStartTime; }
    public void setRegisterStartTime(LocalDateTime registerStartTime) { this.registerStartTime = registerStartTime; }
    public LocalDateTime getRegisterEndTime() { return registerEndTime; }
    public void setRegisterEndTime(LocalDateTime registerEndTime) { this.registerEndTime = registerEndTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }
    public Integer getContestType() { return contestType; }
    public void setContestType(Integer contestType) { this.contestType = contestType; }
    public Integer getTeamMinSize() { return teamMinSize; }
    public void setTeamMinSize(Integer teamMinSize) { this.teamMinSize = teamMinSize; }
    public Integer getTeamMaxSize() { return teamMaxSize; }
    public void setTeamMaxSize(Integer teamMaxSize) { this.teamMaxSize = teamMaxSize; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getCurrentCount() { return currentCount; }
    public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

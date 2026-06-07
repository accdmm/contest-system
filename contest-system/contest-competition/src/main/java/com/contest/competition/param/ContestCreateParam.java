package com.contest.competition.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ContestCreateParam {

    @NotBlank(message = "竞赛名称不能为空")
    private String name;

    @NotBlank(message = "竞赛类别不能为空")
    private String category;

    @NotBlank(message = "竞赛级别不能为空")
    private String level;

    private String organizer;

    @NotNull(message = "竞赛时间不能为空")
    private LocalDateTime contestTime;

    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime registerStartTime;

    @NotNull(message = "报名结束时间不能为空")
    private LocalDateTime registerEndTime;

    private String location;

    private String coverImageUrl;

    private String description;

    private String attachmentUrls;

    @NotNull(message = "竞赛类型不能为空")
    private Integer contestType;

    private Integer teamMinSize;

    private Integer teamMaxSize;

    private Integer maxParticipants;

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
}

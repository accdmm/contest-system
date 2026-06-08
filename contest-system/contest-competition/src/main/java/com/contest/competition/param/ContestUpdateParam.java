package com.contest.competition.param;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ContestUpdateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 竞赛ID */
    @NotNull(message = "竞赛ID不能为空")
    private Long id;

    /** 竞赛名称 */
    @NotBlank(message = "竞赛名称不能为空")
    private String name;

    /** 竞赛类别 */
    @NotBlank(message = "竞赛类别不能为空")
    private String category;

    /** 竞赛级别 */
    @NotBlank(message = "竞赛级别不能为空")
    private String level;

    /** 主办方 */
    private String organizer;

    /** 竞赛时间 */
    @NotNull(message = "竞赛时间不能为空")
    private LocalDateTime contestTime;

    /** 报名开始时间 */
    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime registerStartTime;

    /** 报名结束时间 */
    @NotNull(message = "报名结束时间不能为空")
    private LocalDateTime registerEndTime;

    /** 竞赛地点 */
    private String location;

    /** 封面图片URL */
    private String coverImageUrl;

    /** 竞赛描述 */
    private String description;

    /** 附件URL（多个以逗号分隔） */
    private String attachmentUrls;

    /** 竞赛类型：0-个人赛，1-团队赛，2-两者皆可 */
    @NotNull(message = "竞赛类型不能为空")
    private Integer contestType;

    /** 团队最小人数 */
    private Integer teamMinSize;

    /** 团队最大人数 */
    private Integer teamMaxSize;

    /** 最大参与人数 */
    private Integer maxParticipants;

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
}

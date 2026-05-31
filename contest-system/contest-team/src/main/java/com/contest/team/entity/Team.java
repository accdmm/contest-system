package com.contest.team.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("team")
public class Team {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long leaderId;

    private Long teacherId;

    private String teamName;

    private String teamNo;

    private String inviteCode;

    private LocalDateTime inviteCodeExpire;

    @TableLogic
    private Integer deleted;

    private Integer status;

    private Integer memberCount;

    private String materialUrls;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamNo() { return teamNo; }
    public void setTeamNo(String teamNo) { this.teamNo = teamNo; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public LocalDateTime getInviteCodeExpire() { return inviteCodeExpire; }
    public void setInviteCodeExpire(LocalDateTime inviteCodeExpire) { this.inviteCodeExpire = inviteCodeExpire; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public String getMaterialUrls() { return materialUrls; }
    public void setMaterialUrls(String materialUrls) { this.materialUrls = materialUrls; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

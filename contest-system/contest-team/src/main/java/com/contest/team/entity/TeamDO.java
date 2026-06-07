package com.contest.team.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/** 团队实体 */
@TableName("team")
public class TeamDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 队长用户ID */
    private Long leaderId;

    /** 指导教师用户ID */
    private Long teacherId;

    /** 团队名称 */
    private String teamName;

    /** 团队编号 */
    private String teamNo;

    /** 邀请码 */
    private String inviteCode;

    /** 邀请码过期时间 */
    private LocalDateTime inviteCodeExpire;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    /** 团队状态（组建中/已提交/已通过/已驳回） */
    private Integer status;

    /** 成员人数 */
    private Integer memberCount;

    /** 作品材料URL（逗号分隔） */
    private String materialUrls;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return "TeamDO{id=" + id + ", teamName='" + teamName + "', status=" + status + "}";
    }

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

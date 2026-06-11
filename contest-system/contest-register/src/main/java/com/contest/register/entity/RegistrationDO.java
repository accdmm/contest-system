package com.contest.register.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 报名记录实体 */
@TableName("registration")
public class RegistrationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报名记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 竞赛ID */
    private Long contestId;

    /** 用户ID */
    private Long userId;

    /** 团队ID（团队报名时非空） */
    private Long teamId;

    /** 报名类型：0-个人报名，1-团队报名 */
    private Integer regType;

    /** 状态：0-待审核，1-已通过，2-已驳回，3-已取消 */
    private Integer status;

    /** 驳回原因 */
    private String reviewReason;

    /** 作品材料URL */
    private String materialUrls;

    /** 报名备注 */
    private String remark;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @JsonIgnore
    @TableLogic
    private Integer isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String contestName;

    @TableField(exist = false)
    private String userName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Integer getRegType() { return regType; }
    public void setRegType(Integer regType) { this.regType = regType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getReviewReason() { return reviewReason; }
    public void setReviewReason(String reviewReason) { this.reviewReason = reviewReason; }
    public String getMaterialUrls() { return materialUrls; }
    public void setMaterialUrls(String materialUrls) { this.materialUrls = materialUrls; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getContestName() { return contestName; }
    public void setContestName(String contestName) { this.contestName = contestName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}

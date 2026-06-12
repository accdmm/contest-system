package com.contest.team.param;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/** 创建团队请求参数 */
public class TeamCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 团队名称 */
    @NotBlank(message = "团队名称不能为空")
    private String teamName;

    /** 指导教师用户ID（可选） */
    private Long teacherId;

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}

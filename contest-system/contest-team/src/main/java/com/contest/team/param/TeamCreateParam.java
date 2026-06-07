package com.contest.team.param;

import jakarta.validation.constraints.NotBlank;

public class TeamCreateParam {

    @NotBlank(message = "团队名称不能为空")
    private String teamName;

    private Long teacherId;

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}

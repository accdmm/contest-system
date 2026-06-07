package com.contest.register.param;

import jakarta.validation.constraints.NotNull;

public class RegTeamParam {

    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    @NotNull(message = "团队ID不能为空")
    private Long teamId;

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}

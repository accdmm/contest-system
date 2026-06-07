package com.contest.register.param;

import jakarta.validation.constraints.NotNull;

/** 团队报名参数 */
public class RegTeamParam {

    /** 竞赛ID */
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    /** 团队ID */
    @NotNull(message = "团队ID不能为空")
    private Long teamId;

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}

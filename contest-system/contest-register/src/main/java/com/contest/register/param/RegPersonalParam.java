package com.contest.register.param;

import jakarta.validation.constraints.NotNull;

public class RegPersonalParam {

    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    private String remark;

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

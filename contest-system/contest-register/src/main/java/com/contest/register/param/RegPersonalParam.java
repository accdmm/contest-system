package com.contest.register.param;

import jakarta.validation.constraints.NotNull;

/** 个人报名参数 */
public class RegPersonalParam {

    /** 竞赛ID */
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    /** 报名备注 */
    private String remark;

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

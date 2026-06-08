package com.contest.team.param;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/** 加入团队请求参数 */
public class TeamJoinParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 6位邀请码 */
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}

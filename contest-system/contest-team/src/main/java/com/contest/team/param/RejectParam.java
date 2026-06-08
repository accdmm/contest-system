package com.contest.team.param;

import java.io.Serializable;
import jakarta.validation.constraints.Size;

public class RejectParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(min = 5, max = 500, message = "驳回原因5-500个字符")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

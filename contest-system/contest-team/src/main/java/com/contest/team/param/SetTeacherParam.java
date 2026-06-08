package com.contest.team.param;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class SetTeacherParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}

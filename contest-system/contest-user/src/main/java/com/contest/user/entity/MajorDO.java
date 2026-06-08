package com.contest.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.io.Serializable;

/** 专业实体 */
@TableName("major")
public class MajorDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer collegeId;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCollegeId() { return collegeId; }
    public void setCollegeId(Integer collegeId) { this.collegeId = collegeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

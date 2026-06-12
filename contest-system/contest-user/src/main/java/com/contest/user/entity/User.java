package com.contest.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 用户实体 */
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String name;

    private Integer role;

    private String email;

    private String phone;

    private Integer collegeId;

    private Integer majorId;

    private String college;

    private String major;

    @TableField("class_name")
    private String className;

    private String avatarUrl;

    private Integer status;

    private Integer emailNotify;

    private Integer smsNotify;

    private Integer deadlineNotify;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @JsonIgnore
    @TableLogic
    private Integer isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', name='" + name + "', role=" + role + "}";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getCollegeId() { return collegeId; }
    public void setCollegeId(Integer collegeId) { this.collegeId = collegeId; }
    public Integer getMajorId() { return majorId; }
    public void setMajorId(Integer majorId) { this.majorId = majorId; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getEmailNotify() { return emailNotify; }
    public void setEmailNotify(Integer emailNotify) { this.emailNotify = emailNotify; }
    public Integer getSmsNotify() { return smsNotify; }
    public void setSmsNotify(Integer smsNotify) { this.smsNotify = smsNotify; }
    public Integer getDeadlineNotify() { return deadlineNotify; }
    public void setDeadlineNotify(Integer deadlineNotify) { this.deadlineNotify = deadlineNotify; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

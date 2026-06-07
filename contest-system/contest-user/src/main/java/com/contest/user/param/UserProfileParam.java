package com.contest.user.param;

public class UserProfileParam {

    private String name;

    private String email;

    private String phone;

    private Integer collegeId;

    private Integer majorId;

    private String className;

    private String avatarUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getCollegeId() { return collegeId; }
    public void setCollegeId(Integer collegeId) { this.collegeId = collegeId; }
    public Integer getMajorId() { return majorId; }
    public void setMajorId(Integer majorId) { this.majorId = majorId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

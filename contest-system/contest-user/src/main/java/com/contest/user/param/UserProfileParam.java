package com.contest.user.param;

public class UserProfileParam {

    /** 姓名 */
    private String name;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 学院ID */
    private Integer collegeId;

    /** 专业ID */
    private Integer majorId;

    /** 班级名称 */
    private String className;

    /** 头像URL */
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

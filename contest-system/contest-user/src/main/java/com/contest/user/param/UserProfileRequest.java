package com.contest.user.param;

import java.io.Serializable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 姓名 */
    @Size(min = 1, max = 50, message = "姓名长度1-50个字符")
    private String name;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 学院ID */
    private Integer collegeId;

    /** 专业ID */
    private Integer majorId;

    /** 班级名称 */
    @Size(max = 50, message = "班级名称最长50个字符")
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

package com.contest.user.param;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度8-20个字符")
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

package com.contest.admin.entity;

import com.baomidou.mybatisplus.annotation.*;

@TableName("user_permission")
public class UserPermission {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long userId;

    private Integer permissionId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
}

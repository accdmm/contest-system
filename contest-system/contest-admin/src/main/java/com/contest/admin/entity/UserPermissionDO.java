package com.contest.admin.entity;

import com.baomidou.mybatisplus.annotation.*;

/** 用户-权限关联实体 */
@TableName("user_permission")
public class UserPermissionDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户ID */
    private Long userId;

    /** 权限ID */
    private Integer permissionId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
}

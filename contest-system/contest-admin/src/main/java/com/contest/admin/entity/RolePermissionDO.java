package com.contest.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("role_permission")
public class RolePermissionDO {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer role;
    private Integer permissionId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
}

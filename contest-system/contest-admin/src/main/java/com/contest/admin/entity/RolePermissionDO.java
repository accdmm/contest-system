package com.contest.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/** 角色-权限关联实体 */
@TableName("role_permission")
public class RolePermissionDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    /** 角色 */
    private Integer role;
    /** 权限ID */
    private Integer permissionId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
}

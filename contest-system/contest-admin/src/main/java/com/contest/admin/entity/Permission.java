package com.contest.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/** 权限实体 */
@TableName("permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    /** 权限编码 */
    private String code;
    /** 权限名称 */
    private String name;
    /** 所属模块 */
    private String module;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
}

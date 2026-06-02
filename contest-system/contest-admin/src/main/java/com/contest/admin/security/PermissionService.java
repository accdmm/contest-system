package com.contest.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.admin.entity.Permission;
import com.contest.admin.entity.RolePermission;
import com.contest.admin.entity.UserPermission;
import com.contest.admin.mapper.PermissionMapper;
import com.contest.admin.mapper.RolePermissionMapper;
import com.contest.admin.mapper.UserPermissionMapper;
import com.contest.common.constant.PermissionConstants;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserPermissionMapper userPermissionMapper;

    public PermissionService(RolePermissionMapper rolePermissionMapper, PermissionMapper permissionMapper,
                             UserPermissionMapper userPermissionMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.userPermissionMapper = userPermissionMapper;
    }

    public Set<String> getPermissions(Long userId, Integer role) {
        Set<String> perms = new HashSet<>();
        try {
            List<RolePermission> rps = rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRole, role));
            if (!rps.isEmpty()) {
                Set<Integer> permIds = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
                perms.addAll(permissionMapper.selectBatchIds(permIds).stream()
                        .map(Permission::getCode).collect(Collectors.toSet()));
            } else if (role == 1) {
                perms.addAll(allPermissions());
            }
            if (userId != null) {
                List<UserPermission> ups = userPermissionMapper.selectList(
                        new LambdaQueryWrapper<UserPermission>().eq(UserPermission::getUserId, userId));
                if (!ups.isEmpty()) {
                    Set<Integer> upIds = ups.stream().map(UserPermission::getPermissionId).collect(Collectors.toSet());
                    perms.addAll(permissionMapper.selectBatchIds(upIds).stream()
                            .map(Permission::getCode).collect(Collectors.toSet()));
                }
            }
        } catch (Exception e) {
            if (role == 1) perms.addAll(allPermissions());
        }
        return perms;
    }

    public List<Permission> getAllPermissions() {
        return permissionMapper.selectList(null);
    }

    public List<Integer> getPermissionIdsByRole(Integer role) {
        return rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRole, role)
        ).stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
    }

    public void saveRolePermissions(Integer role, List<Integer> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRole, role));
        for (Integer permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRole(role);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    public List<Integer> getPermissionIdsByUser(Long userId) {
        return userPermissionMapper.selectList(
                new LambdaQueryWrapper<UserPermission>().eq(UserPermission::getUserId, userId)
        ).stream().map(UserPermission::getPermissionId).collect(Collectors.toList());
    }

    public void saveUserPermissions(Long userId, List<Integer> permissionIds) {
        userPermissionMapper.delete(new LambdaQueryWrapper<UserPermission>().eq(UserPermission::getUserId, userId));
        for (Integer permId : permissionIds) {
            UserPermission up = new UserPermission();
            up.setUserId(userId);
            up.setPermissionId(permId);
            userPermissionMapper.insert(up);
        }
    }

    private Set<String> allPermissions() {
        Set<String> perms = new HashSet<>();
        perms.add(PermissionConstants.CONTEST_CREATE);
        perms.add(PermissionConstants.CONTEST_UPDATE);
        perms.add(PermissionConstants.CONTEST_DELETE);
        perms.add(PermissionConstants.CONTEST_PUBLISH);
        perms.add(PermissionConstants.USER_LIST);
        perms.add(PermissionConstants.USER_CREATE);
        perms.add(PermissionConstants.USER_FREEZE);
        perms.add(PermissionConstants.REG_APPROVE);
        perms.add(PermissionConstants.REG_LIST);
        perms.add(PermissionConstants.REG_CANCEL);
        perms.add(PermissionConstants.TEAM_APPROVE);
        perms.add(PermissionConstants.TEAM_LIST);
        perms.add(PermissionConstants.NOTIFY_SEND);
        perms.add(PermissionConstants.NOTIFY_BROADCAST);
        perms.add(PermissionConstants.CMS_CREATE);
        perms.add(PermissionConstants.CMS_UPDATE);
        perms.add(PermissionConstants.CMS_DELETE);
        perms.add(PermissionConstants.LOG_LIST);
        perms.add(PermissionConstants.FILE_UPLOAD);
        perms.add(PermissionConstants.PERMISSION_ASSIGN);
        return perms;
    }
}

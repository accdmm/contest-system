package com.contest.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.admin.entity.PermissionDO;
import com.contest.admin.entity.RolePermissionDO;
import com.contest.admin.entity.UserPermissionDO;
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
            List<RolePermissionDO> rps = rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<RolePermissionDO>().eq(RolePermissionDO::getRole, role));
            if (!rps.isEmpty()) {
                Set<Integer> permIds = rps.stream().map(RolePermissionDO::getPermissionId).collect(Collectors.toSet());
                perms.addAll(permissionMapper.selectBatchIds(permIds).stream()
                        .map(PermissionDO::getCode).collect(Collectors.toSet()));
            } else if (role == 1) {
                perms.addAll(allPermissions());
            }
            if (userId != null) {
                List<UserPermissionDO> ups = userPermissionMapper.selectList(
                        new LambdaQueryWrapper<UserPermissionDO>().eq(UserPermissionDO::getUserId, userId));
                if (!ups.isEmpty()) {
                    Set<Integer> upIds = ups.stream().map(UserPermissionDO::getPermissionId).collect(Collectors.toSet());
                    perms.addAll(permissionMapper.selectBatchIds(upIds).stream()
                            .map(PermissionDO::getCode).collect(Collectors.toSet()));
                }
            }
        } catch (Exception e) {
            if (role == 1) perms.addAll(allPermissions());
        }
        return perms;
    }

    public List<PermissionDO> getAllPermissions() {
        return permissionMapper.selectList(null);
    }

    public List<Integer> getPermissionIdsByRole(Integer role) {
        return rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermissionDO>().eq(RolePermissionDO::getRole, role)
        ).stream().map(RolePermissionDO::getPermissionId).collect(Collectors.toList());
    }

    public void saveRolePermissions(Integer role, List<Integer> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermissionDO>().eq(RolePermissionDO::getRole, role));
        for (Integer permId : permissionIds) {
            RolePermissionDO rp = new RolePermissionDO();
            rp.setRole(role);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    public List<Integer> getPermissionIdsByUser(Long userId) {
        return userPermissionMapper.selectList(
                new LambdaQueryWrapper<UserPermissionDO>().eq(UserPermissionDO::getUserId, userId)
        ).stream().map(UserPermissionDO::getPermissionId).collect(Collectors.toList());
    }

    public void saveUserPermissions(Long userId, List<Integer> permissionIds) {
        userPermissionMapper.delete(new LambdaQueryWrapper<UserPermissionDO>().eq(UserPermissionDO::getUserId, userId));
        for (Integer permId : permissionIds) {
            UserPermissionDO up = new UserPermissionDO();
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

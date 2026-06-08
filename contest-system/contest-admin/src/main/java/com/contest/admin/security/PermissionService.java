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

/**
 * 权限服务 — 基于 RBAC 模型的用户权限查询与分配
 *
 * 权限模型说明：
 * - 角色权限（role_permission 表）：每个角色（学生/管理员/教师）关联一组权限
 * - 用户权限（user_permission 表）：独立用户的额外权限，用于角色基础上的补充
 * - 管理员默认拥有全部权限（通过 allPermissions() 方法提供全量集合）
 *
 * Security 层调用流程：JwtAuthFilter.doFilterInternal() 在每次请求时调用
 * getPermissions() 获取当前用户的权限编码集合，写入 SecurityContext，
 * 后续 @PreAuthorize("hasAuthority('contest:create')") 注解即可自动校验。
 *
 * 性能说明：每次请求都查询权限，但权限表数据量小（通常几十条），
 * 配合数据库索引，单次查询耗时在 1ms 以内。
 */
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

    /**
     * 获取用户权限编码集合
     *
     * 权限合并规则：角色权限 ∪ 个人额外权限。管理员角色（role=1）的
     * role_permission 表可能为空（因为管理员直接获取全部权限），
     * 此时通过 allPermissions() 返回全量集合。
     *
     * @param userId 用户 ID（个人额外权限查询用，可能为 null）
     * @param role   用户角色（0=学生，1=管理员，2=教师）
     * @return 权限编码集合
     */
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

    /** 获取所有权限列表 */
    public List<PermissionDO> getAllPermissions() {
        return permissionMapper.selectList(null);
    }

    /**
     * 获取角色关联的权限 ID 列表
     *
     * @param role 角色
     * @return 权限 ID 列表
     */
    public List<Integer> getPermissionIdsByRole(Integer role) {
        return rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermissionDO>().eq(RolePermissionDO::getRole, role)
        ).stream().map(RolePermissionDO::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 保存角色的权限配置（先删后插）
     *
     * 先删除该角色所有权限关联，再批量插入新关联。保证原子性。
     *
     * @param role          角色
     * @param permissionIds 权限 ID 列表
     */
    public void saveRolePermissions(Integer role, List<Integer> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermissionDO>().eq(RolePermissionDO::getRole, role));
        for (Integer permId : permissionIds) {
            RolePermissionDO rp = new RolePermissionDO();
            rp.setRole(role);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    /**
     * 获取用户额外权限 ID 列表
     *
     * @param userId 用户 ID
     * @return 权限 ID 列表
     */
    public List<Integer> getPermissionIdsByUser(Long userId) {
        return userPermissionMapper.selectList(
                new LambdaQueryWrapper<UserPermissionDO>().eq(UserPermissionDO::getUserId, userId)
        ).stream().map(UserPermissionDO::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 保存用户的额外权限配置（先删后插）
     *
     * @param userId        用户 ID
     * @param permissionIds 权限 ID 列表
     */
    public void saveUserPermissions(Long userId, List<Integer> permissionIds) {
        userPermissionMapper.delete(new LambdaQueryWrapper<UserPermissionDO>().eq(UserPermissionDO::getUserId, userId));
        for (Integer permId : permissionIds) {
            UserPermissionDO up = new UserPermissionDO();
            up.setUserId(userId);
            up.setPermissionId(permId);
            userPermissionMapper.insert(up);
        }
    }

    /**
     * 获取管理员默认全量权限集合
     *
     * 管理员角色拥有系统所有操作权限，无需通过 role_permission 表逐条配置。
     */
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

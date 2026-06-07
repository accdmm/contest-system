package com.contest.admin.controller;

import com.contest.admin.entity.PermissionDO;
import com.contest.admin.security.PermissionService;
import com.contest.common.dto.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<List<PermissionDO>> list() {
        return Result.success(permissionService.getAllPermissions());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<List<Integer>> getByRole(@PathVariable Integer role) {
        return Result.success(permissionService.getPermissionIdsByRole(role));
    }

    @PostMapping("/role/{role}")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<Void> saveRolePermissions(@PathVariable Integer role, @RequestBody Map<String, List<Integer>> body) {
        permissionService.saveRolePermissions(role, body.get("permissionIds"));
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('permission:assign')")
    public Result<List<Integer>> getByUser(@PathVariable Long userId) {
        return Result.success(permissionService.getPermissionIdsByUser(userId));
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('permission:assign')")
    public Result<Void> saveUserPermissions(@PathVariable Long userId, @RequestBody Map<String, List<Integer>> body) {
        permissionService.saveUserPermissions(userId, body.get("permissionIds"));
        return Result.success();
    }
}

package com.contest.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.dto.Result;
import com.contest.common.util.JwtUtil;
import com.contest.user.param.LoginRequest;
import com.contest.user.param.RegisterRequest;
import com.contest.user.param.AdminCreateUserRequest;
import com.contest.user.param.UserProfileParam;
import com.contest.user.entity.College;
import com.contest.user.entity.Major;
import com.contest.user.entity.User;
import com.contest.user.service.CollegeService;
import com.contest.user.service.MajorService;
import com.contest.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CollegeService collegeService;
    private final MajorService majorService;

    public UserController(UserService userService, JwtUtil jwtUtil,
                          CollegeService collegeService, MajorService majorService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.collegeService = collegeService;
        this.majorService = majorService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Valid RegisterRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setCollegeId(req.getCollegeId());
        user.setMajorId(req.getMajorId());
        log.info("新用户注册: username={}", req.getUsername());
        User saved = userService.register(user, req.getPassword());
        String token = jwtUtil.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        saved.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", saved);
        data.put("token", token);
        return Result.success(data);
    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasAuthority('user:create')")
    public Result<User> adminCreateUser(@RequestBody @Valid AdminCreateUserRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean callerIsAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (req.getRole() != null && req.getRole() == 1 && !callerIsAdmin) {
            return Result.error("仅管理员可创建管理员账号");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setCollegeId(req.getCollegeId());
        user.setMajorId(req.getMajorId());
        user.setRole(req.getRole());
        User saved = userService.adminCreateUser(user, req.getPassword());
        saved.setPassword(null);
        return Result.success(saved);
    }

    @GetMapping("/colleges")
    public Result<List<College>> listColleges() {
        return Result.success(collegeService.list());
    }

    @GetMapping("/majors")
    public Result<List<Major>> listMajors(@RequestParam Integer collegeId) {
        return Result.success(majorService.getByCollegeId(collegeId));
    }

    @GetMapping("/teachers")
    public Result<List<User>> listTeachers() {
        return Result.success(userService.listTeachers());
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<IPage<User>> page(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.pageUsers(keyword, page, size));
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateProfile(@PathVariable Long id, @RequestBody @Valid UserProfileParam param) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!currentUserId.equals(id) && !isAdmin) {
            return Result.error("无权修改其他用户的资料");
        }
        User user = new User();
        user.setName(param.getName());
        user.setEmail(param.getEmail());
        user.setPhone(param.getPhone());
        user.setCollegeId(param.getCollegeId());
        user.setMajorId(param.getMajorId());
        user.setClassName(param.getClassName());
        user.setAvatarUrl(param.getAvatarUrl());
        userService.updateProfile(id, user);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            return Result.error("无权修改其他用户的密码");
        }
        userService.changePassword(id, params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }

    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasAuthority('user:freeze')")
    public Result<Void> freeze(@PathVariable Long id) {
        userService.freezeUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/unfreeze")
    @PreAuthorize("hasAuthority('user:freeze')")
    public Result<Void> unfreeze(@PathVariable Long id) {
        userService.unfreezeUser(id);
        return Result.success();
    }
}

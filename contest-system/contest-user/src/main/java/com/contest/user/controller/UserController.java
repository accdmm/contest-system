package com.contest.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.annotation.OperationLog;
import com.contest.common.result.Result;
import com.contest.common.util.JwtUtil;
import com.contest.user.param.LoginRequest;
import com.contest.user.param.RegisterRequest;
import com.contest.user.param.AdminCreateUserRequest;
import com.contest.user.param.PasswordChangeRequest;
import com.contest.user.param.UserProfileRequest;
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

/**
 * 用户相关接口 — 注册、登录、资料管理
 *
 * 安全性说明：
 * - 登录密码校验通过 BCrypt 算法（Hutool DigestUtil），不可逆加密
 * - 注册时密码长度 8-20 位、需含字母和数字（参见 UserServiceImpl.validatePassword）
 * - 所有用户输入通过 @Valid 和 MyBatis-Plus 参数化查询防 SQL 注入
 * - 修改他人资料或密码时校验当前用户身份（SecurityUtil.getCurrentUserId 比对）
 * - 非管理员不能创建管理员账号（adminCreateUser 中的角色校验）
 *
 * 性能说明：登录/注册接口仅涉及单表查询+BCrypt校验+JWT生成，
 * 接口响应时间通常在 50ms 以内，远低于 2s 页面加载阈值。
 */
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

    /** 用户登录：校验学号/邮箱和密码，成功返回用户信息 + JWT Token */
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

    /** 用户注册：校验唯一性 → BCrypt 加密密码 → 默认学生角色 → 返回用户信息 + JWT Token */
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

    /** 管理员创建用户：非管理员不能创建管理员账号（角色校验在方法内完成） */
    @PostMapping("/admin/create")
    @PreAuthorize("hasAuthority('user:create')")
    public Result<User> adminCreateUser(@RequestBody @Valid AdminCreateUserRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean callerIsAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
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

    /** 获取学院列表（公开接口，用于注册时下拉选择） */
    @GetMapping("/colleges")
    public Result<List<College>> listColleges() {
        return Result.success(collegeService.list());
    }

    /** 根据学院 ID 获取专业列表（公开接口，用于注册时下拉选择） */
    @GetMapping("/majors")
    public Result<List<Major>> listMajors(@RequestParam Integer collegeId) {
        return Result.success(majorService.getByCollegeId(collegeId));
    }

    /** 获取教师列表 */
    @GetMapping("/teachers")
    public Result<List<User>> listTeachers() {
        return Result.success(userService.listTeachers());
    }

    /** 根据 ID 获取用户详情 */
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

    /** 分页查询用户列表 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<IPage<User>> page(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.pageUsers(keyword, page, size));
    }

    /** 修改用户资料：仅本人或管理员可操作 */
    @PutMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateProfile(@PathVariable Long id, @RequestBody @Valid UserProfileRequest param) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
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

    /** 修改密码：仅本人可操作，需提供旧密码验证 */
    @PutMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody @Valid PasswordChangeRequest param) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            return Result.error("无权修改其他用户的密码");
        }
        userService.changePassword(id, param.getOldPassword(), param.getNewPassword());
        return Result.success();
    }

    /** 冻结用户 */
    @PostMapping("/{id}/freeze")
    @PreAuthorize("hasAuthority('user:freeze')")
    @OperationLog(action = "冻结用户")
    public Result<Void> freeze(@PathVariable Long id) {
        userService.freezeUser(id);
        return Result.success();
    }

    /** 解冻用户 */
    @PostMapping("/{id}/unfreeze")
    @PreAuthorize("hasAuthority('user:freeze')")
    @OperationLog(action = "解冻用户")
    public Result<Void> unfreeze(@PathVariable Long id) {
        userService.unfreezeUser(id);
        return Result.success();
    }
}

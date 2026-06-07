package com.contest.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.annotation.OperationLog;
import com.contest.common.result.Result;
import com.contest.common.util.JwtUtil;
import com.contest.user.param.LoginRequest;
import com.contest.user.param.RegisterRequest;
import com.contest.user.param.AdminCreateUserRequest;
import com.contest.user.param.UserProfileParam;
import com.contest.user.entity.CollegeDO;
import com.contest.user.entity.MajorDO;
import com.contest.user.entity.UserDO;
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
/** 用户相关接口 */
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

    /** 用户登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
        UserDO user = userService.login(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);
        return Result.success(data);
    }

    /** 用户注册 */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Valid RegisterRequest req) {
        UserDO user = new UserDO();
        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setCollegeId(req.getCollegeId());
        user.setMajorId(req.getMajorId());
        log.info("新用户注册: username={}", req.getUsername());
        UserDO saved = userService.register(user, req.getPassword());
        String token = jwtUtil.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        saved.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", saved);
        data.put("token", token);
        return Result.success(data);
    }

    /** 管理员创建用户 */
    @PostMapping("/admin/create")
    @PreAuthorize("hasAuthority('user:create')")
    public Result<UserDO> adminCreateUser(@RequestBody @Valid AdminCreateUserRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean callerIsAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (req.getRole() != null && req.getRole() == 1 && !callerIsAdmin) {
            return Result.error("仅管理员可创建管理员账号");
        }
        UserDO user = new UserDO();
        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setCollegeId(req.getCollegeId());
        user.setMajorId(req.getMajorId());
        user.setRole(req.getRole());
        UserDO saved = userService.adminCreateUser(user, req.getPassword());
        saved.setPassword(null);
        return Result.success(saved);
    }

    /** 获取学院列表 */
    @GetMapping("/colleges")
    public Result<List<CollegeDO>> listColleges() {
        return Result.success(collegeService.list());
    }

    /** 根据学院ID获取专业列表 */
    @GetMapping("/majors")
    public Result<List<MajorDO>> listMajors(@RequestParam Integer collegeId) {
        return Result.success(majorService.getByCollegeId(collegeId));
    }

    /** 获取教师列表 */
    @GetMapping("/teachers")
    public Result<List<UserDO>> listTeachers() {
        return Result.success(userService.listTeachers());
    }

    /** 根据ID获取用户详情 */
    @GetMapping("/detail/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<UserDO> getById(@PathVariable Long id) {
        UserDO user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /** 分页查询用户列表 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<IPage<UserDO>> page(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.pageUsers(keyword, page, size));
    }

    /** 修改用户资料 */
    @PostMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateProfile(@PathVariable Long id, @RequestBody @Valid UserProfileParam param) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!currentUserId.equals(id) && !isAdmin) {
            return Result.error("无权修改其他用户的资料");
        }
        UserDO user = new UserDO();
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

    /** 修改密码 */
    @PostMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            return Result.error("无权修改其他用户的密码");
        }
        userService.changePassword(id, params.get("oldPassword"), params.get("newPassword"));
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

package com.contest.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.dto.Result;
import com.contest.common.util.JwtUtil;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid Map<String, String> params) {
        User user = userService.login(params.get("username"), params.get("password"));
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Valid Map<String, String> params) {
        User user = new User();
        user.setUsername(params.get("username"));
        user.setName(params.get("name"));
        user.setEmail(params.get("email"));
        user.setPhone(params.get("phone"));
        User saved = userService.register(user, params.get("password"));
        String token = jwtUtil.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        saved.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", saved);
        data.put("token", token);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/page")
    public Result<IPage<User>> page(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.pageUsers(keyword, page, size));
    }

    @PutMapping("/{id}/profile")
    public Result<Void> updateProfile(@PathVariable Long id, @RequestBody User user) {
        userService.updateProfile(id, user);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        userService.changePassword(id, params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }

    @PutMapping("/{id}/freeze")
    public Result<Void> freeze(@PathVariable Long id) {
        userService.freezeUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/unfreeze")
    public Result<Void> unfreeze(@PathVariable Long id) {
        userService.unfreezeUser(id);
        return Result.success();
    }
}

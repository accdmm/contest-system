package com.contest.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.dto.Result;
import com.contest.message.entity.Notification;
import com.contest.message.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<Notification>> byUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            return Result.error("无权查看其他用户的通知");
        }
        return Result.success(notificationService.pageByUser(userId, page, size));
    }

    @GetMapping("/unread/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> unreadCount(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            return Result.error("无权查看其他用户的通知");
        }
        return Result.success(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAllRead(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            return Result.error("无权操作其他用户的通知");
        }
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @PostMapping("/send")
    @PreAuthorize("hasAuthority('notification:send')")
    public Result<Void> send(@RequestParam Long userId, @RequestParam Integer type,
                             @RequestParam String title, @RequestParam String content) {
        notificationService.sendNotification(userId, type, title, content, null, null);
        return Result.success();
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAuthority('notification:broadcast')")
    public Result<Void> broadcast(@RequestParam Integer type, @RequestParam String title,
                                  @RequestParam String content) {
        notificationService.sendBroadcast(type, title, content);
        return Result.success();
    }
}

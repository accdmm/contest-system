package com.contest.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.dto.Result;
import com.contest.message.entity.Notification;
import com.contest.message.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public Result<IPage<Notification>> byUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(notificationService.pageByUser(userId, page, size));
    }

    @GetMapping("/unread/{userId}")
    public Result<Long> unreadCount(@PathVariable Long userId) {
        return Result.success(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all/{userId}")
    public Result<Void> markAllRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @PostMapping("/send")
    public Result<Void> send(@RequestParam Long userId, @RequestParam Integer type,
                             @RequestParam String title, @RequestParam String content) {
        notificationService.sendNotification(userId, type, title, content, null, null);
        return Result.success();
    }

    @PostMapping("/broadcast")
    public Result<Void> broadcast(@RequestParam Integer type, @RequestParam String title,
                                  @RequestParam String content) {
        notificationService.sendBroadcast(type, title, content);
        return Result.success();
    }
}

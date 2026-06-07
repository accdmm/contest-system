package com.contest.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.dto.Result;
import com.contest.message.entity.NotificationDO;
import com.contest.message.param.NotificationSendParam;
import com.contest.message.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<NotificationDO>> byUser(@PathVariable Long userId,
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

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PostMapping("/read-all/{userId}")
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
    public Result<Void> send(@RequestBody @Valid NotificationSendParam param) {
        notificationService.sendNotification(param.getUserId(), param.getType(), param.getTitle(), param.getContent(), param.getRelatedId(), param.getRelatedType());
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

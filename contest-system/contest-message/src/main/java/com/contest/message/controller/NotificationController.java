package com.contest.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.result.Result;
import com.contest.common.util.HtmlSanitizer;
import com.contest.message.entity.NotificationDO;
import com.contest.message.param.NotificationSendParam;
import com.contest.message.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/** 通知消息接口 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** 分页获取用户通知列表 */
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

    /** 获取用户未读通知数量 */
    @GetMapping("/unread/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> unreadCount(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            return Result.error("无权查看其他用户的通知");
        }
        return Result.success(notificationService.countUnread(userId));
    }

    /** 标记单条通知为已读 */
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /** 标记所有通知为已读 */
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

    /** 发送通知给指定用户 */
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('notification:send')")
    public Result<Void> send(@RequestBody @Valid NotificationSendParam param) {
        notificationService.sendNotification(param.getUserId(), param.getType(), param.getTitle(),
                HtmlSanitizer.sanitize(param.getContent()), param.getRelatedId(), param.getRelatedType());
        return Result.success();
    }

    /** 发送广播通知 */
    @PostMapping("/broadcast")
    @PreAuthorize("hasAuthority('notification:broadcast')")
    public Result<Void> broadcast(@RequestParam Integer type, @RequestParam String title,
                                  @RequestParam String content) {
        notificationService.sendBroadcast(type, title, HtmlSanitizer.sanitize(content));
        return Result.success();
    }
}

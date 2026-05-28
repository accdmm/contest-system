package com.contest.register.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.common.constant.CommonConstants;
import com.contest.message.service.NotificationService;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class AdminNotifyService {

    private final UserService userService;
    private final NotificationService notificationService;

    public AdminNotifyService(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public void notifyAdmins(Integer type, String title, String content, Long relatedId, String relatedType) {
        java.util.List<User> admins = userService.list(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        admins.forEach(admin -> notificationService.sendNotification(admin.getId(), type, title, content, relatedId, relatedType));
    }
}

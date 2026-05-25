package com.contest.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.message.entity.Notification;

public interface NotificationService extends IService<Notification> {

    void sendNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType);

    void sendBroadcast(Integer type, String title, String content);

    IPage<Notification> pageByUser(Long userId, Integer page, Integer size);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    long countUnread(Long userId);
}

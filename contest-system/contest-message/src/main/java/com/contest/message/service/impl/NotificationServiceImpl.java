package com.contest.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.message.entity.Notification;
import com.contest.message.mapper.NotificationMapper;
import com.contest.message.service.NotificationService;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 通知服务实现 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(CommonConstants.NOTIFY_UNREAD);
        save(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendBroadcast(Integer type, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(0L);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(CommonConstants.NOTIFY_UNREAD);
        save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<Notification> pageByUser(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .or()
                .eq(Notification::getUserId, 0L)
                .orderByDesc(Notification::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        Notification notification = getById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!Objects.equals(notification.getUserId(), userId) && notification.getUserId() != 0L) {
            throw new BusinessException("无权操作该通知");
        }
        notification.setIsRead(CommonConstants.NOTIFY_READ);
        updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getIsRead, CommonConstants.NOTIFY_UNREAD)
                .and(w -> w.eq(Notification::getUserId, userId)
                           .or()
                           .eq(Notification::getUserId, 0L));
        Notification update = new Notification();
        update.setIsRead(CommonConstants.NOTIFY_READ);
        update(update, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getIsRead, CommonConstants.NOTIFY_UNREAD)
                .and(w -> w.eq(Notification::getUserId, userId)
                           .or()
                           .eq(Notification::getUserId, 0L)));
    }
}

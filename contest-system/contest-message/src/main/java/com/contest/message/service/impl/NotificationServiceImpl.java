package com.contest.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.message.entity.NotificationDO;
import com.contest.message.mapper.NotificationMapper;
import com.contest.message.service.NotificationService;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 通知服务实现 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, NotificationDO> implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType) {
        NotificationDO notification = new NotificationDO();
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
        NotificationDO notification = new NotificationDO();
        notification.setUserId(0L);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(CommonConstants.NOTIFY_UNREAD);
        save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<NotificationDO> pageByUser(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<NotificationDO>()
                .eq(NotificationDO::getUserId, userId)
                .or()
                .eq(NotificationDO::getUserId, 0L)
                .orderByDesc(NotificationDO::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        NotificationDO notification = getById(id);
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
        LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<NotificationDO>()
                .eq(NotificationDO::getIsRead, CommonConstants.NOTIFY_UNREAD)
                .and(w -> w.eq(NotificationDO::getUserId, userId)
                           .or()
                           .eq(NotificationDO::getUserId, 0L));
        NotificationDO update = new NotificationDO();
        update.setIsRead(CommonConstants.NOTIFY_READ);
        update(update, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return count(new LambdaQueryWrapper<NotificationDO>()
                .eq(NotificationDO::getIsRead, CommonConstants.NOTIFY_UNREAD)
                .and(w -> w.eq(NotificationDO::getUserId, userId)
                           .or()
                           .eq(NotificationDO::getUserId, 0L)));
    }
}

package com.contest.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.message.entity.NotificationDO;

/** 通知服务接口 */
public interface NotificationService extends IService<NotificationDO> {

    /** 发送通知给指定用户
     * @param userId 接收用户ID
     * @param type 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @param relatedId 关联业务ID
     * @param relatedType 关联业务类型 */
    void sendNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType);

    /** 发送广播通知（userId=0）
     * @param type 通知类型
     * @param title 通知标题
     * @param content 通知内容 */
    void sendBroadcast(Integer type, String title, String content);

    /** 分页获取用户通知（含广播）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果 */
    IPage<NotificationDO> pageByUser(Long userId, Integer page, Integer size);

    /** 标记单条通知为已读
     * @param id 通知ID
     * @param userId 用户ID（校验归属） */
    void markAsRead(Long id, Long userId);

    /** 标记用户所有通知为已读
     * @param userId 用户ID */
    void markAllAsRead(Long userId);

    /** 统计用户未读通知数
     * @param userId 用户ID
     * @return 未读数 */
    long countUnread(Long userId);
}

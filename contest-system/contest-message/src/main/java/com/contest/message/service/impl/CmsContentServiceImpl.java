package com.contest.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.message.entity.CmsContent;
import com.contest.message.mapper.CmsContentMapper;
import com.contest.message.service.CmsContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CmsContentServiceImpl extends ServiceImpl<CmsContentMapper, CmsContent> implements CmsContentService {

    @Override
    public List<CmsContent> listBanners() {
        LambdaQueryWrapper<CmsContent> wrapper = new LambdaQueryWrapper<CmsContent>()
                .eq(CmsContent::getContentType, CommonConstants.CMS_BANNER)
                .eq(CmsContent::getStatus, CommonConstants.CMS_VISIBLE)
                .orderByAsc(CmsContent::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<CmsContent> listAnnouncements(String position) {
        LambdaQueryWrapper<CmsContent> wrapper = new LambdaQueryWrapper<CmsContent>()
                .eq(CmsContent::getContentType, CommonConstants.CMS_ANNOUNCEMENT)
                .eq(CmsContent::getStatus, CommonConstants.CMS_VISIBLE)
                .and(w -> w.isNull(CmsContent::getPublishTime)
                        .or().le(CmsContent::getPublishTime, LocalDateTime.now()))
                .orderByAsc(CmsContent::getSortOrder)
                .orderByDesc(CmsContent::getCreateTime);
        if (position != null && !position.isEmpty()) {
            wrapper.eq(CmsContent::getPosition, position);
        }
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CmsContent createContent(CmsContent content) {
        log.info("create cms content: type={}, title={}", content.getContentType(), content.getTitle());
        content.setStatus(CommonConstants.CMS_VISIBLE);
        save(content);
        return content;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CmsContent updateContent(CmsContent content) {
        log.info("update cms content: id={}, title={}", content.getId(), content.getTitle());
        content.setPublishTime(LocalDateTime.now());
        updateById(content);
        return content;
    }
}

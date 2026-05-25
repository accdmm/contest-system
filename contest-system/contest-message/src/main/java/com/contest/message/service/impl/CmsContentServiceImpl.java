package com.contest.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.message.entity.CmsContent;
import com.contest.message.mapper.CmsContentMapper;
import com.contest.message.service.CmsContentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    public CmsContent createContent(CmsContent content) {
        content.setStatus(CommonConstants.CMS_VISIBLE);
        save(content);
        return content;
    }

    @Override
    public CmsContent updateContent(CmsContent content) {
        updateById(content);
        return content;
    }
}

package com.contest.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.message.entity.CmsContentDO;
import com.contest.message.mapper.CmsContentMapper;
import com.contest.message.service.CmsContentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 内容管理服务实现 */
@Service
public class CmsContentServiceImpl extends ServiceImpl<CmsContentMapper, CmsContentDO> implements CmsContentService {

    @Override
    public List<CmsContentDO> listBanners() {
        LambdaQueryWrapper<CmsContentDO> wrapper = new LambdaQueryWrapper<CmsContentDO>()
                .eq(CmsContentDO::getContentType, CommonConstants.CMS_BANNER)
                .eq(CmsContentDO::getStatus, CommonConstants.CMS_VISIBLE)
                .orderByAsc(CmsContentDO::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<CmsContentDO> listAnnouncements(String position) {
        LambdaQueryWrapper<CmsContentDO> wrapper = new LambdaQueryWrapper<CmsContentDO>()
                .eq(CmsContentDO::getContentType, CommonConstants.CMS_ANNOUNCEMENT)
                .eq(CmsContentDO::getStatus, CommonConstants.CMS_VISIBLE)
                .and(w -> w.isNull(CmsContentDO::getPublishTime)
                        .or().le(CmsContentDO::getPublishTime, LocalDateTime.now()))
                .orderByAsc(CmsContentDO::getSortOrder)
                .orderByDesc(CmsContentDO::getCreateTime);
        if (position != null && !position.isEmpty()) {
            wrapper.eq(CmsContentDO::getPosition, position);
        }
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CmsContentDO createContent(CmsContentDO content) {
        content.setStatus(CommonConstants.CMS_VISIBLE);
        save(content);
        return content;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CmsContentDO updateContent(CmsContentDO content) {
        content.setPublishTime(LocalDateTime.now());
        updateById(content);
        return content;
    }
}

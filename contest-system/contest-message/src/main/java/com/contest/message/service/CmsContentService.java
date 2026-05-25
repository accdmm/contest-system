package com.contest.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.message.entity.CmsContent;

import java.util.List;

public interface CmsContentService extends IService<CmsContent> {

    List<CmsContent> listBanners();

    List<CmsContent> listAnnouncements(String position);

    CmsContent createContent(CmsContent content);

    CmsContent updateContent(CmsContent content);
}

package com.contest.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.message.entity.CmsContentDO;

import java.util.List;

public interface CmsContentService extends IService<CmsContentDO> {

    List<CmsContentDO> listBanners();

    List<CmsContentDO> listAnnouncements(String position);

    CmsContentDO createContent(CmsContentDO content);

    CmsContentDO updateContent(CmsContentDO content);
}

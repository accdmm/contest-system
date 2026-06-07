package com.contest.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.message.entity.CmsContentDO;

import java.util.List;

/** 内容管理服务接口 */
public interface CmsContentService extends IService<CmsContentDO> {

    /** 获取轮播图列表 */
    List<CmsContentDO> listBanners();

    /** 获取公告列表
     * @param position 位置标识（可选）
     * @return 公告列表 */
    List<CmsContentDO> listAnnouncements(String position);

    /** 创建内容
     * @param content 内容实体
     * @return 创建后的内容 */
    CmsContentDO createContent(CmsContentDO content);

    /** 更新内容
     * @param content 内容实体
     * @return 更新后的内容 */
    CmsContentDO updateContent(CmsContentDO content);
}

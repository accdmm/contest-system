package com.contest.message.controller;

import com.contest.common.result.Result;
import com.contest.common.util.HtmlSanitizer;
import com.contest.message.entity.CmsContent;
import com.contest.message.param.CmsContentCreateRequest;
import com.contest.message.param.CmsContentUpdateRequest;
import com.contest.message.service.CmsContentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 内容管理接口 */
@RestController
@RequestMapping("/api/cms")
public class CmsContentController {

    private final CmsContentService cmsContentService;

    public CmsContentController(CmsContentService cmsContentService) {
        this.cmsContentService = cmsContentService;
    }

    /** 获取轮播图列表 */
    @GetMapping("/banners")
    public Result<List<CmsContent>> banners() {
        return Result.success(cmsContentService.listBanners());
    }

    /** 获取公告列表（可按位置筛选） */
    @GetMapping("/announcements")
    public Result<List<CmsContent>> announcements(@RequestParam(required = false) String position) {
        return Result.success(cmsContentService.listAnnouncements(position));
    }

    /** 创建内容 */
    @PostMapping
    @PreAuthorize("hasAuthority('cms:create')")
    public Result<CmsContent> create(@RequestBody CmsContentCreateRequest param) {
        CmsContent content = new CmsContent();
        content.setContentType(param.getContentType());
        content.setTitle(param.getTitle());
        content.setContent(HtmlSanitizer.sanitize(param.getContent()));
        content.setImageUrl(param.getImageUrl());
        content.setLinkUrl(param.getLinkUrl());
        content.setSortOrder(param.getSortOrder());
        content.setPosition(param.getPosition());
        content.setStatus(param.getStatus());
        return Result.success(cmsContentService.createContent(content));
    }

    /** 更新内容 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:update')")
    public Result<CmsContent> update(@PathVariable Long id, @RequestBody CmsContentUpdateRequest param) {
        param.setId(id);
        CmsContent content = new CmsContent();
        content.setId(param.getId());
        content.setContentType(param.getContentType());
        content.setTitle(param.getTitle());
        content.setContent(HtmlSanitizer.sanitize(param.getContent()));
        content.setImageUrl(param.getImageUrl());
        content.setLinkUrl(param.getLinkUrl());
        content.setSortOrder(param.getSortOrder());
        content.setPosition(param.getPosition());
        content.setStatus(param.getStatus());
        return Result.success(cmsContentService.updateContent(content));
    }

    /** 根据ID获取内容详情 */
    @GetMapping("/{id}")
    public Result<CmsContent> getById(@PathVariable Long id) {
        CmsContent content = cmsContentService.getById(id);
        if (content == null) {
            return Result.error("内容不存在");
        }
        return Result.success(content);
    }

    /** 删除内容 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        cmsContentService.removeById(id);
        return Result.success();
    }
}

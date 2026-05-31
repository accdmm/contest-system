package com.contest.message.controller;

import com.contest.common.dto.Result;
import com.contest.message.entity.CmsContent;
import com.contest.message.service.CmsContentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms")
public class CmsContentController {

    private final CmsContentService cmsContentService;

    public CmsContentController(CmsContentService cmsContentService) {
        this.cmsContentService = cmsContentService;
    }

    @GetMapping("/banners")
    public Result<List<CmsContent>> banners() {
        return Result.success(cmsContentService.listBanners());
    }

    @GetMapping("/announcements")
    public Result<List<CmsContent>> announcements(@RequestParam(required = false) String position) {
        return Result.success(cmsContentService.listAnnouncements(position));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cms:create')")
    public Result<CmsContent> create(@RequestBody CmsContent content) {
        return Result.success(cmsContentService.createContent(content));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('cms:update')")
    public Result<CmsContent> update(@RequestBody CmsContent content) {
        return Result.success(cmsContentService.updateContent(content));
    }

    @GetMapping("/{id}")
    public Result<CmsContent> getById(@PathVariable Long id) {
        CmsContent content = cmsContentService.getById(id);
        if (content == null) {
            return Result.error("内容不存在");
        }
        return Result.success(content);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        cmsContentService.removeById(id);
        return Result.success();
    }
}

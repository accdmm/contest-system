package com.contest.admin.controller;

import com.contest.admin.service.MinioService;
import com.contest.common.result.Result;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件上传与资源访问接口
 *
 * <p>对接 MinIO 对象存储，提供文件上传和资源访问能力。
 * 上传需登录认证，资源访问（GET /api/uploads/**）公开可访问（见 SecurityConfig）。
 *
 * <p>可用性说明：上传返回的 URL 可直接用于前端 img 标签或附件链接，
 * 支持常见图片格式（PNG/JPG/GIF/WebP/SVG）和文档格式（PDF/DOC/ZIP）。
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    private final MinioService minioService;

    public UploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 上传文件
     *
     * <p>用户上传头像、竞赛封面图、附件等场景调用。登录用户可上传。
     *
     * @param file 上传的文件（multipart/form-data 格式）
     * @return 文件访问路径，如 /api/uploads/uuid.jpg
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = minioService.upload(file);
        return Result.success(url);
    }

    /**
     * 获取上传文件（公开访问）
     *
     * <p>安全配置中 /api/uploads/** 已开放公开访问，前端可直接引用返回的 URL。
     * 根据文件扩展名自动设置 Content-Type，浏览器可直接渲染。
     *
     * @param filename 文件名
     * @return 文件流资源
     */
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        InputStream inputStream = minioService.getFile(filename);
        if (inputStream == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(detectContentType(filename)))
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 根据文件扩展名检测 MIME 类型
     *
     * @param filename 文件名
     * @return MIME 类型字符串，未知格式返回 application/octet-stream
     */
    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "application/msword";
        if (lower.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }
}

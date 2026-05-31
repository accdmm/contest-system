package com.contest.admin.controller;

import com.contest.admin.service.MinioService;
import com.contest.common.dto.Result;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final MinioService minioService;

    public UploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = minioService.upload(file);
        return Result.success(url);
    }

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

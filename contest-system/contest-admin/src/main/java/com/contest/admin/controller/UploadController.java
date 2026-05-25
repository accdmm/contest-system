package com.contest.admin.controller;

import com.contest.admin.service.MinioService;
import com.contest.common.dto.Result;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final MinioService minioService;

    public UploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = minioService.upload(file);
        return Result.success(url);
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        File file = minioService.getFile(filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new FileSystemResource(file));
    }
}

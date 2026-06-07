package com.contest.admin.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface MinioService {

    String upload(MultipartFile file);

    InputStream getFile(String filename);
}

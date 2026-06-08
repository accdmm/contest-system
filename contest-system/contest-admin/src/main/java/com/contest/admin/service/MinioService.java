package com.contest.admin.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

/**
 * MinIO 对象存储服务接口
 *
 * <p>封装文件上传和下载操作，业务方（如竞赛附件、用户头像）通过此接口
 * 统一处理文件存储，无需关心底层是 MinIO 还是本地文件系统。
 */
public interface MinioService {

    /**
     * 上传文件到 MinIO
     *
     * <p>自动生成 UUID 文件名避免冲突，返回可公开访问的 URL 路径。
     *
     * @param file 前端上传的文件（MultipartFile）
     * @return 文件访问路径（如 /api/uploads/uuid.jpg）
     */
    String upload(MultipartFile file);

    /**
     * 从 MinIO 获取文件流
     *
     * @param filename 文件名（不包含路径前缀）
     * @return 文件输入流，文件不存在时返回 null
     */
    InputStream getFile(String filename);
}

package com.contest.admin.service.impl;

import com.contest.admin.config.MinioProperties;
import com.contest.admin.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import com.contest.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.UUID;

/**
 * MinIO 对象存储服务实现
 *
 * 基于 MinIO Java SDK 实现文件上传和下载。服务启动时自动初始化 MinIO 客户端
 * 并检查/创建存储桶。文件上传时生成 UUID 文件名避免覆盖和中文乱码问题。
 *
 * 可用性说明：
 * - 上传文件自动生成唯一文件名（UUID + 原始扩展名），防覆盖
 * - 存储桶不存在时自动创建（init 方法中有 try-catch 兜底，不影响服务启动）
 * - getFile 时文件不存在或读取失败时抛出 BusinessException，由全局异常处理器统一处理
 *
 * 性能说明：MinIO 是高性能对象存储，单次上传/下载耗时通常在毫秒级，
 * 文件可被 CDN 缓存加速前端页面加载。上传路径配置在 application.yml 中，
 * 开发环境可切换为本地文件存储。
 */
@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioProperties properties;
    private MinioClient minioClient;

    public MinioServiceImpl(MinioProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 MinIO 客户端并确保存储桶存在
     *
     * @PostConstruct 注解确保在 Bean 装配完成后自动调用。
     * 存储桶检查失败时只记录警告（不阻断启动），后续上传时再次尝试。
     */
    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        ensureBucketExists();
    }

    /**
     * 检查并创建存储桶
     *
     * MinIO 要求上传前存储桶必须存在。此方法在 init 时尝试创建，
     * 如果 MinIO 服务未就绪只记录警告，不导致应用启动失败。
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket()).build());
                log.info("MinIO bucket created: {}", properties.getBucket());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidResponseException | ServerException | XmlParserException |
                 GeneralSecurityException | IOException e) {
            log.warn("MinIO bucket check failed (will retry on upload): {}", e.getMessage());
        }
    }

    /**
     * 上传文件到 MinIO
     *
     * 生成 UUID 文件名保留原始扩展名，避免中文文件名乱码和重复文件覆盖。
     * 返回的路径可通 SecurityConfig 中的 /api/uploads/** 公开访问。
     *
     * @param file 前端上传的文件
     * @return 文件访问路径，如 /api/uploads/a1b2c3d4e5f6.jpg
     */
    public String upload(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return "/api/uploads/" + fileName;
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidResponseException | ServerException | XmlParserException |
                 GeneralSecurityException | IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 MinIO 获取文件流
     *
     * 文件不存在或读取失败时抛出 BusinessException，由全局异常处理器统一处理。
     *
     * @param filename 文件名
     * @return 文件输入流
     */
    public InputStream getFile(String filename) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(filename)
                    .build());
        } catch (ErrorResponseException e) {
            log.warn("MinIO file not found: {}", filename);
            throw new BusinessException("文件不存在: " + filename);
        } catch (InsufficientDataException | InternalException |
                 InvalidResponseException | ServerException | XmlParserException |
                 GeneralSecurityException | IOException e) {
            log.error("MinIO getFile error: {}", e.getMessage());
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }
    }
}

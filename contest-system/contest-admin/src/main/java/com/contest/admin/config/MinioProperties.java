package com.contest.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 对象存储配置属性
 *
 * <p>从 application.yml 的 minio.* 前缀读取配置，包括服务端点、访问密钥、
 * 秘密密钥和默认桶名。支持切换为本地文件系统（见 application-dev.yml）。
 *
 * <p>安全性说明：accessKey 和 secretKey 通过配置注入，不硬编码在代码中，
 * 生产环境应通过环境变量或配置中心管理。
 */
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** MinIO 服务端点，如 http://localhost:9000 */
    private String endpoint;
    /** 访问密钥（Access Key） */
    private String accessKey;
    /** 秘密密钥（Secret Key） */
    private String secretKey;
    /** 默认存储桶名 */
    private String bucket;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
}

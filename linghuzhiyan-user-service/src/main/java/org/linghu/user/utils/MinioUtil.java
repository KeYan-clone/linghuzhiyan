package org.linghu.user.utils;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO操作工具类
 */
@Slf4j
@Component
public class MinioUtil {

    private final MinioClient minioClient;
    
    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    // 用户头像前缀
    private static final String PREFIX_AVATARS = "avatars/";
    
    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 最大文件大小 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public MinioUtil(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 确保bucket存在
     */
    public void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );
        
        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("创建MinIO bucket: {}", bucketName);
        }
    }

    /**
     * 上传用户头像
     */
    public String uploadUserAvatar(MultipartFile file, String userId) throws Exception {
        // 验证文件
        validateImageFile(file);
        
        // 确保bucket存在
        ensureBucketExists();
        
        // 生成文件名
        String fileName = generateAvatarFileName(file, userId);
        String objectName = PREFIX_AVATARS + fileName;
        
        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        }
        
        log.info("上传用户头像成功: userId={}, objectName={}", userId, objectName);
        return objectName;
    }

    /**
     * 删除用户头像
     */
    public void deleteUserAvatar(String objectName) throws Exception {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            log.info("删除用户头像成功: objectName={}", objectName);
        } catch (Exception e) {
            log.warn("删除用户头像失败: objectName={}, error={}", objectName, e.getMessage());
            // 不抛出异常，删除失败不影响业务流程
        }
    }

    /**
     * 获取头像预览URL
     */
    public String getAvatarPreviewUrl(String objectName, int expiry) throws Exception {
        if (objectName == null || !objectName.startsWith(PREFIX_AVATARS)) {
            throw new IllegalArgumentException("非法的头像路径");
        }

        return generatePreviewUrl(objectName, expiry);
    }

    /**
     * 生成文件的临时预览URL
     *
     * @param objectName MinIO中的对象名
     * @param expiryTime URL过期时间(秒)
     * @return 临时访问URL
     * @throws Exception 如果生成URL失败
     */
    public String generatePreviewUrl(String objectName, int expiryTime) throws Exception {
        // 验证对象路径前缀合法性
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IllegalArgumentException("对象路径不能为空");
        }

        if (objectName.contains("../") || objectName.contains("..\\")) {
            throw new IllegalArgumentException("非法的对象路径：不允许使用相对路径");
        }

        if (objectName.startsWith("/") || objectName.startsWith("\\")) {
            throw new IllegalArgumentException("非法的对象路径：不允许以分隔符开头");
        }

        // 根据对象路径确定使用哪个bucket
        String bucketName = determineBucketByObjectPath(objectName);

        // 构建拼接式URL: http://your-minio-server/bucket/object-path
        String baseUrl = minioEndpoint;

        // 移除末尾的斜杠（如果有）
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 构建完整的URL
        String fullUrl = String.format("%s/%s/%s", baseUrl, bucketName, objectName);

        return fullUrl;
    }

    private String determineBucketByObjectPath(String objectName) {
        if (objectName == null) {
            return bucketName;
        }
        // 头像文件应该存储在默认bucket中
        if (objectName.startsWith(PREFIX_AVATARS)) {
            return bucketName;
        }
        // 判断是否为实验资源路径格式：{experimentId}/experiment/...
        if (isExperimentResourcePath(objectName)) {
            return "resource";
        }

        // 判断是否为学生提交路径格式：{studentId}/{experimentId}/{taskId}/...
        if (isStudentSubmissionPath(objectName)) {
            return "submission";
        }

        // 其他情况使用默认bucket
        return bucketName;
    }

    private boolean isExperimentResourcePath(String objectName) {
        // 实验资源路径格式：{experimentId}/experiment/...
        String[] parts = objectName.split("/");
        return parts.length >= 2 && "experiment".equals(parts[1]);
    }

    private boolean isStudentSubmissionPath(String objectName) {
        // 学生提交路径格式：{studentId}/{experimentId}/{taskId}/...
        String[] parts = objectName.split("/");
        return parts.length >= 3 && !objectName.startsWith(PREFIX_AVATARS);
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("只支持JPG、PNG、GIF、WebP格式的图片");
        }
    }

    /**
     * 生成头像文件名
     */
    private String generateAvatarFileName(MultipartFile file, String userId) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return String.format("%s_%s_%s%s", userId, timestamp, UUID.randomUUID().toString().substring(0, 8), extension);
    }
}

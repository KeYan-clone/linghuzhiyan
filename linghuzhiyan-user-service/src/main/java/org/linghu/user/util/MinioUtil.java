package org.linghu.user.util;

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
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expiry, TimeUnit.SECONDS)
                .build()
        );
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

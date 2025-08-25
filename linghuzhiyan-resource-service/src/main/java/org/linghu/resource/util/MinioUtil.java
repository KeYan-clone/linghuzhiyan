package org.linghu.resource.util;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.linghu.resource.config.MinioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO操作工具类
 */
@Slf4j
@Component
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    // 路径分隔符
    private static final String PATH_SEPARATOR = "/";
    
    // 时间戳格式
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmmss";

    @Autowired
    public MinioUtil(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    /**
     * 确保MinIO存储桶存在，如不存在则创建
     */
    public void ensureBucketExists() throws Exception {
        ensureBucketExists(minioConfig.getDefaultBucket());
        ensureBucketExists(minioConfig.getResourceBucket());
        ensureBucketExists(minioConfig.getSubmissionBucket());
    }

    /**
     * 确保指定的bucket存在
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("创建MinIO存储桶: {}", bucketName);
        }
    }

    /**
     * 上传文件到MinIO
     */
    public String uploadFile(InputStream fileStream, long fileSize, String contentType, String extension) throws Exception {
        String objectName = generateObjectName(extension);
        String bucketName = minioConfig.getResourceBucket();
        
        ensureBucketExists(bucketName);

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(fileStream, fileSize, -1)
                .contentType(contentType)
                .build();

        minioClient.putObject(putObjectArgs);
        log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
        
        return objectName;
    }

    /**
     * 上传MultipartFile到MinIO
     */
    public String uploadFile(MultipartFile file, String extension) throws Exception {
        return uploadFile(file.getInputStream(), file.getSize(), file.getContentType(), extension);
    }

    /**
     * 上传资源文件到指定目录
     */
    public String uploadResource(String resourceType, String fileName, InputStream fileStream, 
                                long fileSize, String contentType) throws Exception {
        String objectName = resourceType + PATH_SEPARATOR + generateFileName(fileName);
        String bucketName = minioConfig.getResourceBucket();
        
        ensureBucketExists(bucketName);

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(fileStream, fileSize, -1)
                .contentType(contentType)
                .build();

        minioClient.putObject(putObjectArgs);
        log.info("资源文件上传成功: bucket={}, object={}", bucketName, objectName);
        
        return objectName;
    }

    /**
     * 上传实验资源到指定实验目录
     */
    public String uploadExperimentResource(String experimentId, String fileName, InputStream fileStream,
                                         long fileSize, String contentType, String resourceType) throws Exception {
        String objectName = "experiments" + PATH_SEPARATOR + experimentId + PATH_SEPARATOR + 
                           resourceType + PATH_SEPARATOR + generateFileName(fileName);
        String bucketName = minioConfig.getResourceBucket();
        
        ensureBucketExists(bucketName);

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(fileStream, fileSize, -1)
                .contentType(contentType)
                .build();

        minioClient.putObject(putObjectArgs);
        log.info("实验资源上传成功: bucket={}, object={}", bucketName, objectName);
        
        return objectName;
    }

    /**
     * 上传学生提交文件
     */
    public String uploadSubmissionFile(MultipartFile file, String studentId, String experimentId, String taskId) throws Exception {
        String objectName = "submissions" + PATH_SEPARATOR + studentId + PATH_SEPARATOR + 
                           experimentId + PATH_SEPARATOR + taskId + PATH_SEPARATOR + 
                           generateFileName(file.getOriginalFilename());
        String bucketName = minioConfig.getSubmissionBucket();
        
        ensureBucketExists(bucketName);

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();

        minioClient.putObject(putObjectArgs);
        log.info("学生提交文件上传成功: bucket={}, object={}", bucketName, objectName);
        
        return objectName;
    }

    /**
     * 从MinIO下载文件
     */
    public InputStreamResource downloadFile(String objectName) throws Exception {
        String bucketName = determineBucketByObjectPath(objectName);

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();

        InputStream stream = minioClient.getObject(getObjectArgs);
        return new InputStreamResource(stream);
    }

    /**
     * 从MinIO删除文件
     */
    public void deleteFile(String objectName) throws Exception {
        String bucketName = determineBucketByObjectPath(objectName);

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();

        minioClient.removeObject(removeObjectArgs);
        log.info("文件删除成功: bucket={}, object={}", bucketName, objectName);
    }

    /**
     * 获取文件的预签名下载URL
     */
    public String getPresignedObjectUrl(String objectName, int expiry) throws Exception {
        String bucketName = determineBucketByObjectPath(objectName);
        
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expiry, TimeUnit.SECONDS)
                .build();

        return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * 列出学生提交的文件
     */
    public Iterable<Result<Item>> listStudentSubmissions(String studentId) {
        String prefix = "submissions" + PATH_SEPARATOR + studentId + PATH_SEPARATOR;
        return listObjects(minioConfig.getSubmissionBucket(), prefix);
    }

    /**
     * 列出实验相关的资源
     */
    public Iterable<Result<Item>> listExperimentResources(String experimentId) {
        String prefix = "experiments" + PATH_SEPARATOR + experimentId + PATH_SEPARATOR;
        return listObjects(minioConfig.getResourceBucket(), prefix);
    }

    /**
     * 列出指定前缀的对象
     */
    private Iterable<Result<Item>> listObjects(String bucketName, String prefix) {
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build();

        return minioClient.listObjects(listObjectsArgs);
    }

    /**
     * 根据对象路径确定使用哪个bucket
     */
    private String determineBucketByObjectPath(String objectName) {
        if (objectName.startsWith("submissions" + PATH_SEPARATOR)) {
            return minioConfig.getSubmissionBucket();
        } else if (objectName.startsWith("experiments" + PATH_SEPARATOR) || 
                   objectName.startsWith("document" + PATH_SEPARATOR) ||
                   objectName.startsWith("video" + PATH_SEPARATOR) ||
                   objectName.startsWith("audio" + PATH_SEPARATOR) ||
                   objectName.startsWith("image" + PATH_SEPARATOR) ||
                   objectName.startsWith("archive" + PATH_SEPARATOR) ||
                   objectName.startsWith("code" + PATH_SEPARATOR) ||
                   objectName.startsWith("other" + PATH_SEPARATOR)) {
            return minioConfig.getResourceBucket();
        }
        return minioConfig.getDefaultBucket();
    }

    /**
     * 生成唯一的对象名
     */
    private String generateObjectName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return timestamp + "-" + uuid + (extension.startsWith(".") ? extension : "." + extension);
    }

    /**
     * 生成带时间戳的文件名
     */
    private String generateFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "-" + uuid + "-" + originalFileName;
    }

    /**
     * 从对象路径中提取实验ID
     */
    public String getExperimentIdFromPath(String objectName) {
        if (objectName.startsWith("experiments" + PATH_SEPARATOR)) {
            String[] parts = objectName.split(PATH_SEPARATOR);
            if (parts.length > 1) {
                return parts[1];
            }
        } else if (objectName.startsWith("submissions" + PATH_SEPARATOR)) {
            String[] parts = objectName.split(PATH_SEPARATOR);
            if (parts.length > 2) {
                return parts[2]; // submissions/studentId/experimentId/...
            }
        }
        return null;
    }
}

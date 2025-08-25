package org.linghu.resource.service.impl;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.resource.domain.Resource;
import org.linghu.resource.dto.ResourceDTO;
import org.linghu.resource.dto.ResourceQueryDTO;
import org.linghu.resource.dto.ResourceRequestDTO;
import org.linghu.resource.repository.ResourceRepository;
import org.linghu.resource.service.ResourceService;
import org.linghu.resource.util.FileUtils;
import org.linghu.resource.util.MinioUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资源服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final MinioUtil minioUtil;

    @Override
    @Transactional
    public ResourceDTO uploadResource(MultipartFile file, ResourceRequestDTO requestDTO, String uploader) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空，无法上传");
        }

        try {
            // 确保存储桶存在
            minioUtil.ensureBucketExists();

            // 获取文件信息
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String sanitizedFileName = FileUtils.sanitizeFileName(originalFilename);

            // 自动检测资源类型
            String resourceType = FileUtils.detectResourceType(sanitizedFileName, file.getContentType());
            String objectName;

            // 根据是否有实验ID选择不同的上传路径
            if (requestDTO.getExperimentId() != null) {
                // 上传到实验资源目录
                String uploadResourceType = requestDTO.getUploadType() != null ? 
                    requestDTO.getUploadType() : "resource";
                
                objectName = minioUtil.uploadExperimentResource(
                    requestDTO.getExperimentId(),
                    sanitizedFileName,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType(),
                    uploadResourceType);
            } else {
                // 上传到通用资源目录
                objectName = minioUtil.uploadResource(
                    resourceType.toLowerCase(),
                    sanitizedFileName,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());
            }

            // 创建资源记录
            Resource resource = Resource.builder()
                    .id(UUID.randomUUID().toString())
                    .experimentId(requestDTO.getExperimentId())
                    .resourceType(Resource.ResourceType.valueOf(resourceType))
                    .resourcePath(objectName)
                    .fileName(sanitizedFileName)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .description(requestDTO.getDescription())
                    .uploader(uploader)
                    .isPublic(requestDTO.getIsPublic())
                    .downloadCount(0)
                    .build();

            Resource savedResource = resourceRepository.save(resource);
            log.info("资源上传成功: id={}, fileName={}, uploader={}", 
                    savedResource.getId(), savedResource.getFileName(), uploader);

            return convertToDTO(savedResource);

        } catch (Exception ex) {
            log.error("上传资源失败: fileName={}, uploader={}, error={}", 
                     file.getOriginalFilename(), uploader, ex.getMessage());
            throw new RuntimeException("无法上传文件: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public ResourceDTO uploadStudentSubmission(MultipartFile file, String studentId, 
                                             String experimentId, String taskId, 
                                             ResourceRequestDTO requestDTO) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空，无法上传");
        }

        try {
            // 确保存储桶存在
            minioUtil.ensureBucketExists();

            // 获取文件信息
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String sanitizedFileName = FileUtils.sanitizeFileName(originalFilename);

            // 上传文件到MinIO的学生提交目录
            String objectName = minioUtil.uploadSubmissionFile(file, studentId, experimentId, taskId);

            // 创建资源记录
            Resource resource = Resource.builder()
                    .id(UUID.randomUUID().toString())
                    .experimentId(experimentId)
                    .resourceType(Resource.ResourceType.SUBMISSION)
                    .resourcePath(objectName)
                    .fileName(sanitizedFileName)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .description(requestDTO != null ? requestDTO.getDescription() : "学生提交文件")
                    .uploader(studentId)
                    .isPublic(false) // 学生提交文件默认不公开
                    .downloadCount(0)
                    .build();

            Resource savedResource = resourceRepository.save(resource);
            log.info("学生提交文件上传成功: id={}, fileName={}, studentId={}, experimentId={}", 
                    savedResource.getId(), savedResource.getFileName(), studentId, experimentId);

            return convertToDTO(savedResource);

        } catch (Exception ex) {
            log.error("上传学生提交文件失败: fileName={}, studentId={}, experimentId={}, error={}", 
                     file.getOriginalFilename(), studentId, experimentId, ex.getMessage());
            throw new RuntimeException("无法上传学生提交文件: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ResourceDTO getResourceById(String id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在: " + id));
        return convertToDTO(resource);
    }

    @Override
    public List<ResourceDTO> getResourcesByExperimentId(String experimentId) {
        List<Resource> resources = resourceRepository.findByExperimentId(experimentId);
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getResourcesByExperimentIdAndType(String experimentId, String resourceType) {
        Resource.ResourceType type = Resource.ResourceType.valueOf(resourceType.toUpperCase());
        List<Resource> resources = resourceRepository.findByExperimentIdAndResourceType(experimentId, type);
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getStudentSubmissions(String studentId) {
        try {
            List<ResourceDTO> submissions = resourceRepository.findByUploader(studentId)
                    .stream()
                    .filter(resource -> Resource.ResourceType.SUBMISSION.equals(resource.getResourceType()))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // 同时从MinIO获取文件列表（防止数据库记录丢失）
            Iterable<Result<Item>> minioResults = minioUtil.listStudentSubmissions(studentId);
            for (Result<Item> result : minioResults) {
                try {
                    Item item = result.get();
                    String objectName = item.objectName();

                    // 检查是否已在数据库中
                    boolean existsInDb = submissions.stream()
                            .anyMatch(dto -> objectName.equals(dto.getResourcePath()));

                    if (!existsInDb) {
                        // 创建临时DTO
                        ResourceDTO tempDTO = createTemporaryDTO(item);
                        if (tempDTO != null) {
                            submissions.add(tempDTO);
                        }
                    }
                } catch (Exception e) {
                    log.warn("处理MinIO对象失败: {}", e.getMessage());
                }
            }

            return submissions;

        } catch (Exception ex) {
            log.error("获取学生提交文件失败: studentId={}, error={}", studentId, ex.getMessage());
            throw new RuntimeException("获取学生提交文件失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<ResourceDTO> getStudentSubmissionsByExperiment(String studentId, String experimentId) {
        List<Resource> resources = resourceRepository.findByUploaderAndExperimentId(studentId, experimentId);
        return resources.stream()
                .filter(resource -> Resource.ResourceType.SUBMISSION.equals(resource.getResourceType()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteResource(String id, String currentUser) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在: " + id));

        // 权限检查：只有上传者或管理员可以删除
        if (!resource.getUploader().equals(currentUser)) {
            // 这里可以添加管理员权限检查
            throw new RuntimeException("无权限删除此资源");
        }

        try {
            // 从MinIO删除文件
            minioUtil.deleteFile(resource.getResourcePath());
            log.info("从MinIO删除文件成功: {}", resource.getResourcePath());
        } catch (Exception ex) {
            log.error("从MinIO删除文件失败: {}, error={}", resource.getResourcePath(), ex.getMessage());
            // 继续删除数据库记录
        }

        // 删除数据库记录
        resourceRepository.delete(resource);
        log.info("删除资源成功: id={}, fileName={}, uploader={}", 
                resource.getId(), resource.getFileName(), currentUser);
    }

    @Override
    public List<ResourceDTO> getPublicResources() {
        List<Resource> resources = resourceRepository.findByIsPublicTrue();
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ResourceDTO> queryResources(ResourceQueryDTO queryDTO) {
        Pageable pageable = createPageable(queryDTO);
        
        // 根据查询条件构建查询
        List<Resource> resources;
        
        if (StringUtils.hasText(queryDTO.getExperimentId())) {
            if (StringUtils.hasText(queryDTO.getResourceType())) {
                Resource.ResourceType type = Resource.ResourceType.valueOf(queryDTO.getResourceType().toUpperCase());
                resources = resourceRepository.findByExperimentIdAndResourceType(queryDTO.getExperimentId(), type);
            } else {
                resources = resourceRepository.findByExperimentId(queryDTO.getExperimentId());
            }
        } else if (StringUtils.hasText(queryDTO.getResourceType())) {
            Resource.ResourceType type = Resource.ResourceType.valueOf(queryDTO.getResourceType().toUpperCase());
            resources = resourceRepository.findByResourceType(type);
        } else if (StringUtils.hasText(queryDTO.getUploader())) {
            resources = resourceRepository.findByUploader(queryDTO.getUploader());
        } else if (StringUtils.hasText(queryDTO.getFileName())) {
            resources = resourceRepository.findByFileNameContaining(queryDTO.getFileName());
        } else if (queryDTO.getPublicOnly()) {
            resources = resourceRepository.findByIsPublicTrue();
        } else {
            resources = resourceRepository.findAll(Sort.by(Sort.Direction.fromString(queryDTO.getSortDirection()), queryDTO.getSortBy()));
        }

        // 转换为DTO
        List<ResourceDTO> dtos = resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        if (start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        
        List<ResourceDTO> pageContent = dtos.subList(start, end);
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }

    @Override
    public org.springframework.core.io.Resource downloadResource(String id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在: " + id));

        try {
            // 增加下载次数
            incrementDownloadCount(id);
            
            // 从MinIO下载文件
            return minioUtil.downloadFile(resource.getResourcePath());
        } catch (Exception ex) {
            log.error("下载资源失败: id={}, fileName={}, error={}", 
                     id, resource.getFileName(), ex.getMessage());
            throw new RuntimeException("下载文件失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getDownloadUrl(String id, String currentUser) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在: " + id));

        // 权限检查
        if (!resource.getIsPublic() && !resource.getUploader().equals(currentUser)) {
            throw new RuntimeException("无权限访问此资源");
        }

        try {
            // 生成预签名URL，有效期1小时
            return minioUtil.getPresignedObjectUrl(resource.getResourcePath(), 3600);
        } catch (Exception ex) {
            log.error("获取下载URL失败: id={}, error={}", id, ex.getMessage());
            throw new RuntimeException("获取下载URL失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public void incrementDownloadCount(String id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在: " + id));
        
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resourceRepository.save(resource);
    }

    @Override
    public List<ResourceDTO> getResourcesByUploader(String uploader) {
        List<Resource> resources = resourceRepository.findByUploader(uploader);
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getRecentlyUploadedResources(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Resource> resources = resourceRepository.findRecentlyUploaded(pageable);
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getPopularResources(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Resource> resources = resourceRepository.findPopularResources(pageable);
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> searchResources(String keyword) {
        List<Resource> resources = resourceRepository.findByFileNameContaining(keyword);
        
        // 同时搜索描述
        List<Resource> descriptionResults = resourceRepository.findByDescriptionContaining(keyword);
        
        // 合并结果并去重
        resources.addAll(descriptionResults);
        List<ResourceDTO> uniqueResults = resources.stream()
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return uniqueResults;
    }

    /**
     * 转换为DTO
     */
    private ResourceDTO convertToDTO(Resource resource) {
        return ResourceDTO.builder()
                .id(resource.getId())
                .experimentId(resource.getExperimentId())
                .resourceType(resource.getResourceType().name())
                .resourcePath(resource.getResourcePath())
                .fileName(resource.getFileName())
                .fileSize(resource.getFileSize())
                .fileSizeFormatted(FileUtils.formatFileSize(resource.getFileSize()))
                .mimeType(resource.getMimeType())
                .description(resource.getDescription())
                .uploader(resource.getUploader())
                .isPublic(resource.getIsPublic())
                .downloadCount(resource.getDownloadCount())
                .uploadTime(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    /**
     * 根据MinIO对象元数据创建临时资源DTO
     */
    private ResourceDTO createTemporaryDTO(Item item) {
        try {
            String objectName = item.objectName();
            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

            // 从路径中提取实验ID
            String experimentId = minioUtil.getExperimentIdFromPath(objectName);

            // 转换上传时间
            LocalDateTime uploadTime = LocalDateTime.now();
            if (item.lastModified() != null) {
                uploadTime = item.lastModified().toLocalDateTime();
            }

            return ResourceDTO.builder()
                    .id("temp-" + UUID.randomUUID().toString())
                    .experimentId(experimentId)
                    .resourceType("SUBMISSION")
                    .resourcePath(objectName)
                    .fileName(fileName)
                    .fileSize(item.size())
                    .fileSizeFormatted(FileUtils.formatFileSize(item.size()))
                    .mimeType(FileUtils.getMimeTypeFromFileName(fileName))
                    .description("从MinIO同步的临时记录")
                    .uploadTime(uploadTime)
                    .build();
        } catch (Exception e) {
            log.error("创建临时DTO失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(ResourceQueryDTO queryDTO) {
        Sort.Direction direction = Sort.Direction.fromString(queryDTO.getSortDirection());
        Sort sort = Sort.by(direction, queryDTO.getSortBy());
        return PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
    }
}

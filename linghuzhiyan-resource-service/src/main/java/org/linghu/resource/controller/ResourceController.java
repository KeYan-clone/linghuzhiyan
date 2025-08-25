package org.linghu.resource.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.resource.dto.ResourceDTO;
import org.linghu.resource.dto.ResourceQueryDTO;
import org.linghu.resource.dto.ResourceRequestDTO;
import org.linghu.resource.service.ResourceService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 资源管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * 上传资源文件
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "experimentId", required = false) String experimentId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
            @RequestParam(value = "uploadType", defaultValue = "resource") String uploadType,
            @RequestParam(value = "autoExtract", defaultValue = "false") Boolean autoExtract,
            Authentication authentication) {
        
        try {
            ResourceRequestDTO requestDTO = ResourceRequestDTO.builder()
                    .experimentId(experimentId)
                    .description(description)
                    .isPublic(isPublic)
                    .uploadType(uploadType)
                    .autoExtract(autoExtract)
                    .build();

            ResourceDTO result = resourceService.uploadResource(file, requestDTO, authentication.getName());
            
            log.info("资源上传成功: id={}, fileName={}, uploader={}", 
                    result.getId(), result.getFileName(), authentication.getName());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("资源上传失败: fileName={}, uploader={}, error={}", 
                     file.getOriginalFilename(), authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 上传学生提交文件
     */
    @PostMapping("/submission")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> uploadStudentSubmission(
            @RequestParam("file") MultipartFile file,
            @RequestParam("experimentId") String experimentId,
            @RequestParam("taskId") String taskId,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        try {
            ResourceRequestDTO requestDTO = ResourceRequestDTO.builder()
                    .description(description)
                    .isPublic(false) // 学生提交默认不公开
                    .build();

            ResourceDTO result = resourceService.uploadStudentSubmission(
                    file, authentication.getName(), experimentId, taskId, requestDTO);
            
            log.info("学生提交文件上传成功: id={}, fileName={}, studentId={}, experimentId={}", 
                    result.getId(), result.getFileName(), authentication.getName(), experimentId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("学生提交文件上传失败: fileName={}, studentId={}, experimentId={}, error={}", 
                     file.getOriginalFilename(), authentication.getName(), experimentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据ID获取资源详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable String id) {
        try {
            ResourceDTO resource = resourceService.getResourceById(id);
            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            log.error("获取资源详情失败: id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据实验ID获取资源列表
     */
    @GetMapping("/experiment/{experimentId}")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getResourcesByExperimentId(@PathVariable String experimentId) {
        try {
            List<ResourceDTO> resources = resourceService.getResourcesByExperimentId(experimentId);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取实验资源列表失败: experimentId={}, error={}", experimentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据实验ID和资源类型获取资源列表
     */
    @GetMapping("/experiment/{experimentId}/type/{resourceType}")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getResourcesByExperimentIdAndType(
            @PathVariable String experimentId,
            @PathVariable String resourceType) {
        try {
            List<ResourceDTO> resources = resourceService.getResourcesByExperimentIdAndType(experimentId, resourceType);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取实验资源列表失败: experimentId={}, resourceType={}, error={}", 
                     experimentId, resourceType, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取学生提交的文件列表
     */
    @GetMapping("/submissions/my")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getMySubmissions(Authentication authentication) {
        try {
            List<ResourceDTO> submissions = resourceService.getStudentSubmissions(authentication.getName());
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("获取学生提交文件列表失败: studentId={}, error={}", 
                     authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取指定学生的提交文件列表（教师用）
     */
    @GetMapping("/submissions/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getStudentSubmissions(@PathVariable String studentId) {
        try {
            List<ResourceDTO> submissions = resourceService.getStudentSubmissions(studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("获取学生提交文件列表失败: studentId={}, error={}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取学生在指定实验中的提交文件
     */
    @GetMapping("/submissions/student/{studentId}/experiment/{experimentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getStudentSubmissionsByExperiment(
            @PathVariable String studentId,
            @PathVariable String experimentId) {
        try {
            List<ResourceDTO> submissions = resourceService.getStudentSubmissionsByExperiment(studentId, experimentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("获取学生实验提交文件失败: studentId={}, experimentId={}, error={}", 
                     studentId, experimentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable String id, Authentication authentication) {
        try {
            resourceService.deleteResource(id, authentication.getName());
            log.info("资源删除成功: id={}, operator={}", id, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除资源失败: id={}, operator={}, error={}", id, authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取公开资源列表
     */
    @GetMapping("/public")
    public ResponseEntity<List<ResourceDTO>> getPublicResources() {
        try {
            List<ResourceDTO> resources = resourceService.getPublicResources();
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取公开资源列表失败: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 分页查询资源
     */
    @PostMapping("/query")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ResourceDTO>> queryResources(@Valid @RequestBody ResourceQueryDTO queryDTO) {
        try {
            Page<ResourceDTO> resources = resourceService.queryResources(queryDTO);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("查询资源失败: queryDTO={}, error={}", queryDTO, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 下载资源
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadResource(@PathVariable String id, Authentication authentication) {
        try {
            ResourceDTO resourceDTO = resourceService.getResourceById(id);
            
            // 权限检查
            if (!resourceDTO.getIsPublic() && 
                (authentication == null || !resourceDTO.getUploader().equals(authentication.getName()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = resourceService.downloadResource(id);
            
            // 设置响应头
            String encodedFileName = URLEncoder.encode(resourceDTO.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + resourceDTO.getFileName() + "\"; filename*=UTF-8''" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, resourceDTO.getMimeType());
            
            log.info("资源下载开始: id={}, fileName={}, downloader={}", 
                    id, resourceDTO.getFileName(), 
                    authentication != null ? authentication.getName() : "anonymous");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            log.error("下载资源失败: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取资源下载URL
     */
    @GetMapping("/download-url/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<String> getDownloadUrl(@PathVariable String id, Authentication authentication) {
        try {
            String downloadUrl = resourceService.getDownloadUrl(id, authentication.getName());
            return ResponseEntity.ok(downloadUrl);
        } catch (Exception e) {
            log.error("获取下载URL失败: id={}, user={}, error={}", id, authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据上传者获取资源列表
     */
    @GetMapping("/uploader/{uploader}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getResourcesByUploader(@PathVariable String uploader) {
        try {
            List<ResourceDTO> resources = resourceService.getResourcesByUploader(uploader);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取用户资源列表失败: uploader={}, error={}", uploader, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取我的资源列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> getMyResources(Authentication authentication) {
        try {
            List<ResourceDTO> resources = resourceService.getResourcesByUploader(authentication.getName());
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取我的资源列表失败: uploader={}, error={}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取最近上传的资源
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ResourceDTO>> getRecentlyUploadedResources(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ResourceDTO> resources = resourceService.getRecentlyUploadedResources(limit);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取最近上传资源失败: limit={}, error={}", limit, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取热门资源
     */
    @GetMapping("/popular")
    public ResponseEntity<List<ResourceDTO>> getPopularResources(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ResourceDTO> resources = resourceService.getPopularResources(limit);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("获取热门资源失败: limit={}, error={}", limit, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 搜索资源
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ResourceDTO>> searchResources(@RequestParam String keyword) {
        try {
            List<ResourceDTO> resources = resourceService.searchResources(keyword);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            log.error("搜索资源失败: keyword={}, error={}", keyword, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

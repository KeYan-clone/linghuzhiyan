package org.linghu.resource.controller;

import org.linghu.resource.dto.ResourceDTO;
import org.linghu.resource.dto.ResourceRequestDTO;
import org.linghu.resource.dto.Result;
import org.linghu.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 资源管理API控制器
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_TEACHER','ROLE_ADMIN','ROLE_ASSISTANT')")
    public Result<ResourceDTO> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam String experimentId,
            @RequestParam String taskId,
            @RequestParam(required = false) String description,
            @RequestParam String uploadType,
            @RequestParam(required = false, defaultValue = "true") Boolean autoExtract) {

        ResourceRequestDTO requestDTO = ResourceRequestDTO.builder()
                .experimentId(experimentId)
                .taskId(taskId)
                .description(description)
                .uploadType(uploadType)
                .autoExtract(autoExtract)
                .build();

        ResourceDTO resource = resourceService.uploadResource(file, requestDTO);
        return Result.success(resource);
    }

    @GetMapping("")
    public Result<List<ResourceDTO>> getAllResources() {
        List<ResourceDTO> resources = resourceService.getAllResources();
        return Result.success(resources);
    }

    @GetMapping("/experiments/{expId}")
    public Result<List<ResourceDTO>> getExperimentResources(@PathVariable(value = "expId")  String expId) {
        List<ResourceDTO> resources = resourceService.getResourcesByExperimentId(expId);
        return Result.success(resources);
    }

    @GetMapping("/{resourceId}")
    public Result<ResourceDTO> getResource(@PathVariable String resourceId) {
        ResourceDTO resource = resourceService.getResourceById(resourceId);
        return Result.success(resource);
    }

    @PutMapping("/{resourceId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER','ROLE_ADMIN','ROLE_ASSISTANT')")
    public Result<ResourceDTO> updateResource(
            @PathVariable String resourceId,
            @RequestBody ResourceRequestDTO requestDTO) {
        ResourceDTO updatedResource = resourceService.updateResource(resourceId, requestDTO);
        return Result.success(updatedResource);
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','ASSISTANT')")
    public Result<Void> deleteResource(@PathVariable String resourceId) {
        resourceService.deleteResource(resourceId);
        return Result.success();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResource(@PathVariable String id) {
        ResourceDTO resourceDTO = resourceService.getResourceById(id);
        Resource fileResource = resourceService.downloadResource(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceDTO.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resourceDTO.getMimeType()))
                .body(fileResource);
    }

    // 以下为学生提交相关接口

    @GetMapping("/submissions/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','ASSISTANT') or #studentId == authentication.principal.id")
    public Result<List<ResourceDTO>> getStudentSubmissions(@PathVariable String studentId) {
        List<ResourceDTO> submissions = resourceService.getStudentSubmissions(studentId);
        return Result.success(submissions);
    }

    @GetMapping("/submissions/student/{studentId}/experiment/{expId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','ASSISTANT') or #studentId == authentication.name")
    public Result<List<ResourceDTO>> getStudentExperimentSubmissions(
            @PathVariable String studentId,
            @PathVariable String expId) {
        List<ResourceDTO> submissions = resourceService.getStudentSubmissionsByExperiment(studentId, expId);
        return Result.success(submissions);
    }


}

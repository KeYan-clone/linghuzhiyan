package org.linghu.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.resource.dto.ResourceDTO;
import org.linghu.resource.dto.ResourceRequestDTO;
import org.linghu.resource.exception.GlobalExceptionHandler;
import org.linghu.resource.service.ResourceService;
import org.linghu.resource.config.JwtTokenProvider;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private ResourceDTO sampleResourceDTO;
    private ResourceRequestDTO sampleRequestDTO;

    @BeforeEach
    void setUp() {
        sampleResourceDTO = ResourceDTO.builder()
                .id("resource-123")
                .experimentId("exp-123")
                .resourceType("DOCUMENT")
                .resourcePath("/files/test.pdf")
                .fileName("test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .description("Test resource")
                .uploadTime(LocalDateTime.now())
                .build();

        sampleRequestDTO = ResourceRequestDTO.builder()
                .experimentId("exp-123")
                .taskId("task-123")
                .resourceType("DOCUMENT")
                .description("Test resource")
                .uploadType("resource")
                .autoExtract(true)
                .build();
    }

    // ========== uploadResource 测试用例 ==========

    @Test
    @WithMockUser(roles = {"TEACHER"})
    void uploadResource_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());
        
        when(resourceService.uploadResource(any(MockMultipartFile.class), any(ResourceRequestDTO.class)))
                .thenReturn(sampleResourceDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("description", "Test resource")
                        .param("uploadType", "resource")
                        .param("resourceType", "DOCUMENT")
                        .param("autoExtract", "true")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value("resource-123"))
                .andExpect(jsonPath("$.data.fileName").value("test.pdf"));

        verify(resourceService).uploadResource(any(MockMultipartFile.class), any(ResourceRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void uploadResource_AccessDenied() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("uploadType", "resource")
                        .param("resourceType", "DOCUMENT")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(resourceService, never()).uploadResource(any(), any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void uploadResource_MissingRequiredParams() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        // Act & Assert - 缺少 experimentId
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("taskId", "task-123")
                        .param("uploadType", "resource")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void uploadResource_ServiceException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());
        
        when(resourceService.uploadResource(any(MockMultipartFile.class), any(ResourceRequestDTO.class)))
                .thenThrow(new RuntimeException("Upload failed"));

        // Act & Assert
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("resourceType", "DOCUMENT")
                        .param("uploadType", "resource")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void uploadResource_InvalidFileType() throws Exception {
        // Arrange - 可能不支持的文件类型
        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "fake virus".getBytes());

        when(resourceService.uploadResource(any(MockMultipartFile.class), any(ResourceRequestDTO.class)))
                .thenThrow(new RuntimeException("不支持的文件类型"));

        // Act & Assert
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("uploadType", "resource")
                        .param("resourceType", "OTHER")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void uploadResource_LargeFile() throws Exception {
        // Arrange - 大文件测试
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeContent);

        when(resourceService.uploadResource(any(MockMultipartFile.class), any(ResourceRequestDTO.class)))
                .thenReturn(sampleResourceDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(largeFile)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("uploadType", "resource")
                        .param("resourceType", "DOCUMENT")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("resource-123"));
    }

    @Test
    void uploadResource_Unauthorized() throws Exception {
        // Arrange - 未登录用户
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("uploadType", "resource")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(resourceService, never()).uploadResource(any(), any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void uploadResource_MissingFile() throws Exception {
        // Act & Assert - 缺少文件
        mockMvc.perform(multipart("/api/resources/upload")
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("uploadType", "resource")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void uploadResource_InvalidUploadType() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        when(resourceService.uploadResource(any(MockMultipartFile.class), any(ResourceRequestDTO.class)))
                .thenReturn(sampleResourceDTO);

        // Act & Assert - 无效的上传类型
        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("experimentId", "exp-123")
                        .param("taskId", "task-123")
                        .param("uploadType", "INVALID_TYPE")
                        .param("resourceType", "DOCUMENT")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ========== getAllResources 测试用例 ==========

    @Test
    @WithMockUser
    void getAllResources_Success() throws Exception {
        // Arrange
        List<ResourceDTO> resources = Collections.singletonList(sampleResourceDTO);
        when(resourceService.getAllResources()).thenReturn(resources);

        // Act & Assert
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("resource-123"));

        verify(resourceService).getAllResources();
    }

    @Test
    @WithMockUser
    void getAllResources_EmptyList() throws Exception {
        // Arrange
        when(resourceService.getAllResources()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser
    void getAllResources_ServiceException() throws Exception {
        // Arrange
        when(resourceService.getAllResources()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllResources_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());

        verify(resourceService, never()).getAllResources();
    }

    @Test
    @WithMockUser
    void getAllResources_LargeDataset() throws Exception {
        // Arrange - 大量数据测试
        List<ResourceDTO> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(ResourceDTO.builder()
                    .id("resource-" + i)
                    .experimentId("exp-123")
                    .fileName("file" + i + ".pdf")
                    .build());
        }
        when(resourceService.getAllResources()).thenReturn(largeList);

        // Act & Assert
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1000));
    }

    // ========== getExperimentResources 测试用例 ==========

    @Test
    @WithMockUser
    void getExperimentResources_Success() throws Exception {
        // Arrange
        List<ResourceDTO> resources = Collections.singletonList(sampleResourceDTO);
        when(resourceService.getResourcesByExperimentId("exp-123")).thenReturn(resources);

        // Act & Assert
        mockMvc.perform(get("/api/resources/experiments/exp-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].experimentId").value("exp-123"));

        verify(resourceService).getResourcesByExperimentId("exp-123");
    }

    @Test
    @WithMockUser
    void getExperimentResources_NotFound() throws Exception {
        // Arrange
        when(resourceService.getResourcesByExperimentId("non-existent"))
                .thenThrow(new RuntimeException("Experiment not found"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/experiments/non-existent"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getExperimentResources_EmptyExperimentId() throws Exception {
        // Act & Assert - 空的实验ID
        mockMvc.perform(get("/api/resources/experiments/"))
                .andExpect(status().isNotFound()); // 路径不匹配
    }

    @Test
    void getExperimentResources_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(get("/api/resources/experiments/exp-123"))
                .andExpect(status().isUnauthorized());
    }

    // ========== getResource 测试用例 ==========

    @Test
    @WithMockUser
    void getResource_Success() throws Exception {
        // Arrange
        when(resourceService.getResourceById("resource-123")).thenReturn(sampleResourceDTO);

        // Act & Assert
        mockMvc.perform(get("/api/resources/resource-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("resource-123"));

        verify(resourceService).getResourceById("resource-123");
    }

    @Test
    @WithMockUser
    void getResource_NotFound() throws Exception {
        // Arrange
        when(resourceService.getResourceById("non-existent"))
                .thenThrow(new RuntimeException("Resource not found"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/non-existent"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getResource_EmptyId() throws Exception {
        // Arrange
        when(resourceService.getAllResources()).thenReturn(Collections.singletonList(sampleResourceDTO));

        // Act & Assert - 空ID会匹配到getAllResources
        mockMvc.perform(get("/api/resources/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getResource_LongId() throws Exception {
        // Arrange - 超长ID测试
        String longId = "a".repeat(1000);
        when(resourceService.getResourceById(longId))
                .thenThrow(new RuntimeException("资源ID无效"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/" + longId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getResource_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(get("/api/resources/resource-123"))
                .andExpect(status().isUnauthorized());
    }

    // ========== updateResource 测试用例 ==========

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateResource_Success() throws Exception {
        // Arrange
        ResourceDTO updatedResource = ResourceDTO.builder()
                .id("resource-123")
                .experimentId("exp-123")
                .description("Updated description")
                .build();
        
        when(resourceService.updateResource(eq("resource-123"), any(ResourceRequestDTO.class)))
                .thenReturn(updatedResource);

        // Act & Assert
        mockMvc.perform(put("/api/resources/resource-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequestDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("resource-123"));

        verify(resourceService).updateResource(eq("resource-123"), any(ResourceRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateResource_AccessDenied() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/resources/resource-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequestDTO))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(resourceService, never()).updateResource(anyString(), any());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateResource_InvalidRequest() throws Exception {
        // Arrange
        ResourceRequestDTO invalidRequest = ResourceRequestDTO.builder()
                .experimentId(null) // 必填字段为空
                .build();

        when(resourceService.updateResource(eq("resource-123"), any(ResourceRequestDTO.class)))
                .thenReturn(sampleResourceDTO);

        // Act & Assert
        mockMvc.perform(put("/api/resources/resource-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateResource_NullRequestBody() throws Exception {
        // Act & Assert - 空请求体
        mockMvc.perform(put("/api/resources/resource-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "TEACHER")
    void updateResource_ResourceNotFound() throws Exception {
        // Arrange
        when(resourceService.updateResource(eq("non-existent"), any(ResourceRequestDTO.class)))
                .thenThrow(new RuntimeException("资源不存在"));

        // Act & Assert
        mockMvc.perform(put("/api/resources/non-existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequestDTO))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateResource_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(put("/api/resources/resource-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequestDTO))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ========== deleteResource 测试用例 ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteResource_Success() throws Exception {
        // Arrange
        doNothing().when(resourceService).deleteResource("resource-123");

        // Act & Assert
        mockMvc.perform(delete("/api/resources/resource-123")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(resourceService).deleteResource("resource-123");
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void deleteResource_AccessDenied() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/resources/resource-123")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(resourceService, never()).deleteResource(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteResource_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Resource not found"))
                .when(resourceService).deleteResource("non-existent");

        // Act & Assert
        mockMvc.perform(delete("/api/resources/non-existent")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteResource_ResourceInUse() throws Exception {
        // Arrange - 资源正在使用中
        doThrow(new RuntimeException("资源正在使用中，无法删除"))
                .when(resourceService).deleteResource("resource-in-use");

        // Act & Assert
        mockMvc.perform(delete("/api/resources/resource-in-use")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteResource_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(delete("/api/resources/resource-123")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deleteResource_TeacherHasPermission() throws Exception {
        // Arrange
        doNothing().when(resourceService).deleteResource("resource-123");

        // Act & Assert - 根据@PreAuthorize配置，TEACHER有权限
        mockMvc.perform(delete("/api/resources/resource-123")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(resourceService).deleteResource("resource-123");
    }

    // ========== downloadResource 测试用例 ==========

    @Test
    @WithMockUser
    void downloadResource_Success() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        Resource fileResource = new ByteArrayResource(fileContent);
        
        when(resourceService.getResourceById("resource-123")).thenReturn(sampleResourceDTO);
        when(resourceService.downloadResource("resource-123")).thenReturn(fileResource);

        // Act & Assert
        mockMvc.perform(get("/api/resources/resource-123/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    "attachment; filename=\"test.pdf\""))
                .andExpect(content().contentType("application/pdf"));

        verify(resourceService).getResourceById("resource-123");
        verify(resourceService).downloadResource("resource-123");
    }

    @Test
    @WithMockUser
    void downloadResource_NotFound() throws Exception {
        // Arrange
        when(resourceService.getResourceById("non-existent"))
                .thenThrow(new RuntimeException("资源不存在"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/non-existent/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void downloadResource_EmptyFile() throws Exception {
        // Arrange - 空文件
        ResourceDTO emptyFileResource = ResourceDTO.builder()
                .id("empty-resource")
                .fileName("empty.txt")
                .mimeType("text/plain")
                .fileSize(0L)
                .build();

        Resource emptyResource = new ByteArrayResource(new byte[0]);

        when(resourceService.getResourceById("empty-resource")).thenReturn(emptyFileResource);
        when(resourceService.downloadResource("empty-resource")).thenReturn(emptyResource);

        // Act & Assert
        mockMvc.perform(get("/api/resources/empty-resource/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                    "attachment; filename=\"empty.txt\""))
                .andExpect(content().contentType("text/plain"));
    }

    @Test
    @WithMockUser
    void downloadResource_SpecialCharactersInFilename() throws Exception {
        // Arrange - 文件名包含特殊字符
        ResourceDTO specialFileResource = ResourceDTO.builder()
                .id("special-resource")
                .fileName("测试文件(1).pdf")
                .mimeType("application/pdf")
                .build();

        Resource fileResource = new ByteArrayResource("content".getBytes());

        when(resourceService.getResourceById("special-resource")).thenReturn(specialFileResource);
        when(resourceService.downloadResource("special-resource")).thenReturn(fileResource);

        // Act & Assert
        mockMvc.perform(get("/api/resources/special-resource/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                    "attachment; filename=\"测试文件(1).pdf\""));
    }

    @Test
    @WithMockUser
    void downloadResource_FileNotFoundOnDisk() throws Exception {
        // Arrange - 文件记录存在但磁盘文件不存在
        when(resourceService.getResourceById("missing-file")).thenReturn(sampleResourceDTO);
        when(resourceService.downloadResource("missing-file"))
                .thenThrow(new RuntimeException("文件不存在"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/missing-file/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void downloadResource_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(get("/api/resources/resource-123/download"))
                .andExpect(status().isUnauthorized());
    }

    // ========== getStudentSubmissions 测试用例 ==========

    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentSubmissions_Success_AsTeacher() throws Exception {
        // Arrange
        List<ResourceDTO> submissions = Collections.singletonList(sampleResourceDTO);
        when(resourceService.getStudentSubmissions("student-123")).thenReturn(submissions);

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/student-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(resourceService).getStudentSubmissions("student-123");
    }





    @Test
    @WithMockUser(roles = "ADMIN")
    void getStudentSubmissions_EmptyStudentId() throws Exception {
        // Act & Assert - 空学生ID
        mockMvc.perform(get("/api/resources/submissions/student/"))
                .andExpect(status().isNotFound()); // 路径不匹配
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentSubmissions_StudentNotExists() throws Exception {
        // Arrange - 学生不存在
        when(resourceService.getStudentSubmissions("non-existent-student"))
                .thenThrow(new RuntimeException("学生不存在"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/non-existent-student"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getStudentSubmissions_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(get("/api/resources/submissions/student/student-123"))
                .andExpect(status().isUnauthorized());
    }

    // ========== getStudentExperimentSubmissions 测试用例 ==========

    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentExperimentSubmissions_Success() throws Exception {
        // Arrange
        List<ResourceDTO> submissions = Collections.singletonList(sampleResourceDTO);
        when(resourceService.getStudentSubmissionsByExperiment("student-123", "exp-123"))
                .thenReturn(submissions);

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/student-123/experiment/exp-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(resourceService).getStudentSubmissionsByExperiment("student-123", "exp-123");
    }

    @Test
    @WithMockUser(username = "student-123", roles = {"STUDENT"})
    void getStudentExperimentSubmissions_Success_AsOwner() throws Exception {
        // Arrange
        List<ResourceDTO> submissions = Collections.singletonList(sampleResourceDTO);
        when(resourceService.getStudentSubmissionsByExperiment("student-123", "exp-123"))
                .thenReturn(submissions);

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/student-123/experiment/exp-123"))
                .andExpect(status().isOk());

        verify(resourceService).getStudentSubmissionsByExperiment("student-123", "exp-123");
    }



    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getStudentExperimentSubmissions_EmptyResult() throws Exception {
        // Arrange
        when(resourceService.getStudentSubmissionsByExperiment("student-123", "exp-123"))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/student-123/experiment/exp-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentExperimentSubmissions_InvalidExperimentId() throws Exception {
        // Arrange - 无效的实验ID
        when(resourceService.getStudentSubmissionsByExperiment("student-123", "invalid-exp"))
                .thenThrow(new RuntimeException("实验不存在"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/student-123/experiment/invalid-exp"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStudentExperimentSubmissions_NullParameters() throws Exception {
        // Arrange
        when(resourceService.getStudentSubmissionsByExperiment("null", "null"))
                .thenReturn(Collections.emptyList());

        // Act & Assert - 路径参数为null，Spring会将null作为字符串处理
        mockMvc.perform(get("/api/resources/submissions/student/null/experiment/null"))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentExperimentSubmissions_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户
        mockMvc.perform(get("/api/resources/submissions/student/student-123/experiment/exp-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student-123", roles = "STUDENT")
    void getStudentExperimentSubmissions_MultipleResults() throws Exception {
        // Arrange - 多个提交记录
        List<ResourceDTO> multipleSubmissions = Arrays.asList(
            sampleResourceDTO,
            ResourceDTO.builder()
                .id("resource-456")
                .experimentId("exp-123")
                .fileName("submission2.pdf")
                .build(),
            ResourceDTO.builder()
                .id("resource-789")
                .experimentId("exp-123")
                .fileName("submission3.pdf")
                .build()
        );
        when(resourceService.getStudentSubmissionsByExperiment("student-123", "exp-123"))
                .thenReturn(multipleSubmissions);

        // Act & Assert
        mockMvc.perform(get("/api/resources/submissions/student/student-123/experiment/exp-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value("resource-123"))
                .andExpect(jsonPath("$.data[1].id").value("resource-456"))
                .andExpect(jsonPath("$.data[2].id").value("resource-789"));
    }

    // ========== 边界和异常情况测试 ==========

    @Test
    @WithMockUser(roles = "TEACHER")
    void controller_HandlesNullPointerException() throws Exception {
        // Arrange - 模拟NPE
        when(resourceService.getAllResources()).thenThrow(new NullPointerException("Null pointer"));

        // Act & Assert
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void controller_HandlesIllegalArgumentException() throws Exception {
        // Arrange - 模拟参数异常
        when(resourceService.getResourceById("invalid-id"))
                .thenThrow(new IllegalArgumentException("Invalid resource ID"));

        // Act & Assert
        mockMvc.perform(get("/api/resources/invalid-id"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void controller_HandlesServiceUnavailable() throws Exception {
        // Arrange - 模拟服务不可用
        when(resourceService.getAllResources())
                .thenThrow(new RuntimeException("服务暂时不可用"));

        // Act & Assert
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isInternalServerError());
    }
}

package org.linghu.resource.service.impl;

import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.resource.client.ExperimentServiceClient;
import org.linghu.resource.domain.Resource;
import org.linghu.resource.dto.ExperimentDTO;
import org.linghu.resource.dto.ResourceDTO;
import org.linghu.resource.dto.ResourceRequestDTO;
import org.linghu.resource.repository.ResourceRepository;
import org.linghu.resource.utils.MinioUtil;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ResourceServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private ExperimentServiceClient experimentServiceClient;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private Resource mockResource;
    private ResourceDTO mockResourceDTO;
    private ResourceRequestDTO mockRequestDTO;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockResource = Resource.builder()
                .id("test-resource-1")
                .experimentId("exp-001")
                .resourceType(Resource.ResourceType.DOCUMENT)
                .resourcePath("/path/to/test.pdf")
                .fileName("test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .description("测试文档")
                .uploadTime(LocalDateTime.now())
                .build();

        mockResourceDTO = ResourceDTO.builder()
                .id("test-resource-1")
                .experimentId("exp-001")
                .resourceType("DOCUMENT")
                .resourcePath("/path/to/test.pdf")
                .fileName("test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .description("测试文档")
                .uploadTime(LocalDateTime.now())
                .build();

        mockRequestDTO = ResourceRequestDTO.builder()
                .experimentId("exp-001")
                .description("测试文档")
                .uploadType("resource")
                .autoExtract(false)
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
    }

    // ==================== uploadResource 测试 ====================

    @Test
    void uploadResource_ShouldReturnResourceDTO_WhenValidFile() throws Exception {
        // Given
        when(experimentServiceClient.experimentExists("exp-001")).thenReturn(true);
        when(minioUtil.uploadExperimentResource(
                eq("exp-001"), 
                eq("test.pdf"), 
                any(), 
                eq(12L), 
                eq("application/pdf"), 
                eq("resource")))
                .thenReturn("/path/to/test.pdf");
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // When
        ResourceDTO result = resourceService.uploadResource(mockFile, mockRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("test.pdf");
        assertThat(result.getExperimentId()).isEqualTo("exp-001");
        
        verify(experimentServiceClient).experimentExists("exp-001");
        verify(minioUtil).ensureBucketExists();
        verify(minioUtil).uploadExperimentResource(
                eq("exp-001"), 
                eq("test.pdf"), 
                any(), 
                eq(12L), 
                eq("application/pdf"), 
                eq("resource"));
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void uploadResource_ShouldThrowException_WhenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        // When & Then
        assertThatThrownBy(() -> resourceService.uploadResource(emptyFile, mockRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("文件为空，无法上传");

        verify(experimentServiceClient, never()).experimentExists(anyString());
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void uploadResource_ShouldThrowException_WhenExperimentNotExists() {
        // Given
        when(experimentServiceClient.experimentExists("exp-001")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> resourceService.uploadResource(mockFile, mockRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("验证实验信息失败: 实验不存在: exp-001");

        verify(experimentServiceClient).experimentExists("exp-001");
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void uploadResource_ShouldThrowException_WhenExperimentValidationFails() {
        // Given
        when(experimentServiceClient.experimentExists("exp-001"))
                .thenThrow(new RuntimeException("网络连接失败"));

        // When & Then
        assertThatThrownBy(() -> resourceService.uploadResource(mockFile, mockRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("验证实验信息失败: 网络连接失败");

        verify(experimentServiceClient).experimentExists("exp-001");
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    // ==================== getResourcesByExperimentId 测试 ====================

    @Test
    void getResourcesByExperimentId_ShouldReturnResourceList_WhenResourcesExist() {
        // Given
        List<Resource> resources = Arrays.asList(mockResource);
        when(resourceRepository.findByExperimentId("exp-001")).thenReturn(resources);

        // When
        List<ResourceDTO> result = resourceService.getResourcesByExperimentId("exp-001");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("test-resource-1");
        assertThat(result.get(0).getExperimentId()).isEqualTo("exp-001");

        verify(resourceRepository).findByExperimentId("exp-001");
    }

    @Test
    void getResourcesByExperimentId_ShouldReturnEmptyList_WhenNoResourcesExist() {
        // Given
        when(resourceRepository.findByExperimentId("non-existent-exp")).thenReturn(new ArrayList<>());

        // When
        List<ResourceDTO> result = resourceService.getResourcesByExperimentId("non-existent-exp");

        // Then
        assertThat(result).isEmpty();

        verify(resourceRepository).findByExperimentId("non-existent-exp");
    }

    // ==================== getResourceById 测试 ====================

    @Test
    void getResourceById_ShouldReturnResourceDTO_WhenResourceExists() {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));

        // When
        ResourceDTO result = resourceService.getResourceById("test-resource-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-resource-1");
        assertThat(result.getFileName()).isEqualTo("test.pdf");

        verify(resourceRepository).findById("test-resource-1");
    }

    @Test
    void getResourceById_ShouldThrowException_WhenResourceNotExists() {
        // Given
        when(resourceRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resourceService.getResourceById("non-existent-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("资源不存在");

        verify(resourceRepository).findById("non-existent-id");
    }

    // ==================== deleteResource 测试 ====================

    @Test
    void deleteResource_ShouldDeleteSuccessfully_WhenResourceExists() throws Exception {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        doNothing().when(minioUtil).deleteFile("/path/to/test.pdf");
        doNothing().when(resourceRepository).delete(mockResource);

        // When
        resourceService.deleteResource("test-resource-1");

        // Then
        verify(resourceRepository).findById("test-resource-1");
        verify(minioUtil).deleteFile("/path/to/test.pdf");
        verify(resourceRepository).delete(mockResource);
    }

    @Test
    void deleteResource_ShouldThrowException_WhenResourceNotExists() throws Exception {
        // Given
        when(resourceRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resourceService.deleteResource("non-existent-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("资源不存在");

        verify(resourceRepository).findById("non-existent-id");
        verify(minioUtil, never()).deleteFile(anyString());
        verify(resourceRepository, never()).delete(any(Resource.class));
    }

    @Test
    void deleteResource_ShouldStillDeleteFromDB_WhenMinioDeleteFails() throws Exception {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        doThrow(new RuntimeException("MinIO删除失败")).when(minioUtil).deleteFile("/path/to/test.pdf");
        doNothing().when(resourceRepository).delete(mockResource);

        // When
        resourceService.deleteResource("test-resource-1");

        // Then
        verify(resourceRepository).findById("test-resource-1");
        verify(minioUtil).deleteFile("/path/to/test.pdf");
        verify(resourceRepository).delete(mockResource); // 仍然删除数据库记录
    }

    // ==================== getAllResources 测试 ====================

    @Test
    void getAllResources_ShouldReturnAllResources_WhenResourcesExist() {
        // Given
        List<Resource> resources = Arrays.asList(mockResource);
        when(resourceRepository.findAll()).thenReturn(resources);

        // When
        List<ResourceDTO> result = resourceService.getAllResources();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("test-resource-1");

        verify(resourceRepository).findAll();
    }

    @Test
    void getAllResources_ShouldReturnEmptyList_WhenNoResourcesExist() {
        // Given
        when(resourceRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<ResourceDTO> result = resourceService.getAllResources();

        // Then
        assertThat(result).isEmpty();

        verify(resourceRepository).findAll();
    }

    // ==================== updateResource 测试 ====================

    @Test
    void updateResource_ShouldUpdateSuccessfully_WhenValidRequest() {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        when(experimentServiceClient.experimentExists("exp-002")).thenReturn(true);
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        ResourceRequestDTO updateRequest = ResourceRequestDTO.builder()
                .experimentId("exp-002")
                .description("更新后的描述")
                .build();

        // When
        ResourceDTO result = resourceService.updateResource("test-resource-1", updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(resourceRepository).findById("test-resource-1");
        verify(experimentServiceClient).experimentExists("exp-002");
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateResource_ShouldThrowException_WhenResourceNotExists() {
        // Given
        when(resourceRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resourceService.updateResource("non-existent-id", mockRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("资源不存在");

        verify(resourceRepository).findById("non-existent-id");
        verify(experimentServiceClient, never()).experimentExists(anyString());
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void updateResource_ShouldThrowException_WhenExperimentNotExists() {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        when(experimentServiceClient.experimentExists("exp-002")).thenReturn(false);

        ResourceRequestDTO updateRequest = ResourceRequestDTO.builder()
                .experimentId("exp-002")
                .build();

        // When & Then
        assertThatThrownBy(() -> resourceService.updateResource("test-resource-1", updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("验证实验信息失败: 实验不存在: exp-002");

        verify(resourceRepository).findById("test-resource-1");
        verify(experimentServiceClient).experimentExists("exp-002");
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    // ==================== downloadResource 测试 ====================

    @Test
    void downloadResource_ShouldReturnResource_WhenResourceExists() throws Exception {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        InputStreamResource fileResource = new InputStreamResource(new ByteArrayInputStream("file content".getBytes()));
        when(minioUtil.downloadFile("/path/to/test.pdf")).thenReturn(fileResource);

        // When
        org.springframework.core.io.Resource result = resourceService.downloadResource("test-resource-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(InputStreamResource.class);

        verify(resourceRepository).findById("test-resource-1");
        verify(minioUtil).downloadFile("/path/to/test.pdf");
    }

    @Test
    void downloadResource_ShouldThrowException_WhenResourceNotExists()throws Exception {
        // Given
        when(resourceRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resourceService.downloadResource("non-existent-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("资源不存在");

        verify(resourceRepository).findById("non-existent-id");
        verify(minioUtil, never()).downloadFile(anyString());
    }

    @Test
    void downloadResource_ShouldThrowException_WhenMinioDownloadFails() throws Exception {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        when(minioUtil.downloadFile("/path/to/test.pdf"))
                .thenThrow(new RuntimeException("MinIO下载失败"));

        // When & Then
        assertThatThrownBy(() -> resourceService.downloadResource("test-resource-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("从MinIO下载文件失败: MinIO下载失败");

        verify(resourceRepository).findById("test-resource-1");
        verify(minioUtil).downloadFile("/path/to/test.pdf");
    }

    // ==================== generatePreviewUrl 测试 ====================

    @Test
    void generatePreviewUrl_ShouldReturnUrl_WhenResourceExists() throws Exception {
        // Given
        when(resourceRepository.findById("test-resource-1")).thenReturn(Optional.of(mockResource));
        when(minioUtil.generatePreviewUrl("/path/to/test.pdf", 3600))
                .thenReturn("https://minio.example.com/preview/test.pdf");

        // When
        String result = resourceService.generatePreviewUrl("test-resource-1", 3600);

        // Then
        assertThat(result).isEqualTo("https://minio.example.com/preview/test.pdf");

        verify(resourceRepository).findById("test-resource-1");
        verify(minioUtil).generatePreviewUrl("/path/to/test.pdf", 3600);
    }

    @Test
    void generatePreviewUrl_ShouldThrowException_WhenResourceNotExists()throws Exception {
        // Given
        when(resourceRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resourceService.generatePreviewUrl("non-existent-id", 3600))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("资源不存在");

        verify(resourceRepository).findById("non-existent-id");
        verify(minioUtil, never()).generatePreviewUrl(anyString(), anyInt());
    }

    // ==================== uploadStudentSubmission 测试 ====================

    @Test
    void uploadStudentSubmission_ShouldReturnResourceDTO_WhenValidSubmission() throws Exception {
        // Given
        when(experimentServiceClient.experimentExists("exp-001")).thenReturn(true);
        when(minioUtil.uploadStudentSubmission(anyString(), anyString(), anyString(), anyString(), any(), anyLong(), anyString()))
                .thenReturn("/submissions/student1/exp-001/task1/test.pdf");
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // When
        ResourceDTO result = resourceService.uploadStudentSubmission(
                mockFile, "student1", "exp-001", "task1", mockRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("test.pdf");

        verify(experimentServiceClient).experimentExists("exp-001");
        verify(minioUtil).ensureBucketExists();
        verify(minioUtil).uploadStudentSubmission(eq("student1"), eq("exp-001"), eq("task1"), 
                eq("test.pdf"), any(), eq(12L), eq("application/pdf"));
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void uploadStudentSubmission_ShouldThrowException_WhenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        // When & Then
        assertThatThrownBy(() -> resourceService.uploadStudentSubmission(
                emptyFile, "student1", "exp-001", "task1", mockRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("文件为空，无法上传");

        verify(experimentServiceClient, never()).experimentExists(anyString());
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    // ==================== getStudentSubmissions 测试 ====================

    @Test
    void getStudentSubmissions_ShouldReturnSubmissions_WhenSubmissionsExist() throws Exception {
        // Given
        List<Result<Item>> mockResults = new ArrayList<>();
        Item mockItem = mock(Item.class);
        Result<Item> mockResult = mock(Result.class);
        when(mockResult.get()).thenReturn(mockItem);
        when(mockItem.objectName()).thenReturn("/submissions/student1/exp-001/test.pdf");
        mockResults.add(mockResult);

        when(minioUtil.listStudentSubmissions("student1")).thenReturn(mockResults);
        when(resourceRepository.findByResourcePath("/submissions/student1/exp-001/test.pdf")).thenReturn(Arrays.asList(mockResource));

        // When
        List<ResourceDTO> result = resourceService.getStudentSubmissions("student1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("test-resource-1");

        verify(minioUtil).listStudentSubmissions("student1");
        verify(resourceRepository).findByResourcePath("/submissions/student1/exp-001/test.pdf");
    }

    @Test
    void getStudentSubmissions_ShouldCreateTemporaryDTO_WhenNoResourceInDatabase() throws Exception {
        // Given
        List<Result<Item>> mockResults = new ArrayList<>();
        Item mockItem = mock(Item.class);
        Result<Item> mockResult = mock(Result.class);
        when(mockResult.get()).thenReturn(mockItem);
        when(mockItem.objectName()).thenReturn("/submissions/student1/exp-001/test.pdf");
        when(mockItem.size()).thenReturn(1024L);
        when(mockItem.lastModified()).thenReturn(ZonedDateTime.now());
        mockResults.add(mockResult);

        when(minioUtil.listStudentSubmissions("student1")).thenReturn(mockResults);
        when(minioUtil.getExperimentIdFromPath("/submissions/student1/exp-001/test.pdf")).thenReturn("exp-001");
        when(resourceRepository.findByResourcePath("/submissions/student1/exp-001/test.pdf")).thenReturn(new ArrayList<>());

        // When
        List<ResourceDTO> result = resourceService.getStudentSubmissions("student1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("test.pdf");
        assertThat(result.get(0).getExperimentId()).isEqualTo("exp-001");
        assertThat(result.get(0).getResourceType()).isEqualTo("SUBMISSION");

        verify(minioUtil).listStudentSubmissions("student1");
        verify(minioUtil).getExperimentIdFromPath("/submissions/student1/exp-001/test.pdf");
        verify(resourceRepository).findByResourcePath("/submissions/student1/exp-001/test.pdf");
    }

    @Test
    void getStudentSubmissions_ShouldThrowException_WhenMinioListFails() throws Exception {
        // Given
        when(minioUtil.listStudentSubmissions("student1"))
                .thenThrow(new RuntimeException("MinIO列表失败"));

        // When & Then
        assertThatThrownBy(() -> resourceService.getStudentSubmissions("student1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("获取学生提交列表失败: MinIO列表失败");

        verify(minioUtil).listStudentSubmissions("student1");
    }

    // ==================== validateExperiment 测试 ====================

    @Test
    void validateExperiment_ShouldReturnTrue_WhenExperimentExists() {
        // Given
        when(experimentServiceClient.experimentExists("exp-001")).thenReturn(true);

        // When
        Boolean result = resourceService.validateExperiment("exp-001");

        // Then
        assertThat(result).isTrue();
        verify(experimentServiceClient).experimentExists("exp-001");
    }

    @Test
    void validateExperiment_ShouldReturnFalse_WhenExperimentNotExists() {
        // Given
        when(experimentServiceClient.experimentExists("non-existent-exp")).thenReturn(false);

        // When
        Boolean result = resourceService.validateExperiment("non-existent-exp");

        // Then
        assertThat(result).isFalse();
        verify(experimentServiceClient).experimentExists("non-existent-exp");
    }

    @Test
    void validateExperiment_ShouldThrowException_WhenClientFails() {
        // Given
        when(experimentServiceClient.experimentExists("exp-001"))
                .thenThrow(new RuntimeException("网络连接失败"));

        // When & Then
        assertThatThrownBy(() -> resourceService.validateExperiment("exp-001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("验证实验信息失败: 网络连接失败");

        verify(experimentServiceClient).experimentExists("exp-001");
    }

    // ==================== getExperimentInfo 测试 ====================

    @Test
    void getExperimentInfo_ShouldReturnExperimentDTO_WhenExperimentExists() {
        // Given
        ExperimentDTO mockExperimentDTO = ExperimentDTO.builder()
                .id("exp-001")
                .name("测试实验")
                .description("测试实验描述")
                .build();
        when(experimentServiceClient.getExperimentById("exp-001")).thenReturn(mockExperimentDTO);

        // When
        ExperimentDTO result = resourceService.getExperimentInfo("exp-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("exp-001");
        assertThat(result.getName()).isEqualTo("测试实验");

        verify(experimentServiceClient).getExperimentById("exp-001");
    }

    @Test
    void getExperimentInfo_ShouldThrowException_WhenClientFails() {
        // Given
        when(experimentServiceClient.getExperimentById("exp-001"))
                .thenThrow(new RuntimeException("网络连接失败"));

        // When & Then
        assertThatThrownBy(() -> resourceService.getExperimentInfo("exp-001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("获取实验信息失败: 网络连接失败");

        verify(experimentServiceClient).getExperimentById("exp-001");
    }
}

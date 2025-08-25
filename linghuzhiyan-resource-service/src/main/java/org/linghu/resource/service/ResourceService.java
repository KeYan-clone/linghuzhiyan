package org.linghu.resource.service;

import org.linghu.resource.dto.ResourceDTO;
import org.linghu.resource.dto.ResourceQueryDTO;
import org.linghu.resource.dto.ResourceRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 资源服务接口
 */
public interface ResourceService {

    /**
     * 上传资源
     */
    ResourceDTO uploadResource(MultipartFile file, ResourceRequestDTO requestDTO, String uploader);

    /**
     * 上传学生提交文件
     */
    ResourceDTO uploadStudentSubmission(MultipartFile file, String studentId, 
                                       String experimentId, String taskId, 
                                       ResourceRequestDTO requestDTO);

    /**
     * 根据ID获取资源
     */
    ResourceDTO getResourceById(String id);

    /**
     * 根据实验ID获取资源列表
     */
    List<ResourceDTO> getResourcesByExperimentId(String experimentId);

    /**
     * 根据实验ID和资源类型获取资源列表
     */
    List<ResourceDTO> getResourcesByExperimentIdAndType(String experimentId, String resourceType);

    /**
     * 获取学生提交的文件列表
     */
    List<ResourceDTO> getStudentSubmissions(String studentId);

    /**
     * 获取学生在指定实验中的提交文件
     */
    List<ResourceDTO> getStudentSubmissionsByExperiment(String studentId, String experimentId);

    /**
     * 删除资源
     */
    void deleteResource(String id, String currentUser);

    /**
     * 获取所有公开资源
     */
    List<ResourceDTO> getPublicResources();

    /**
     * 分页查询资源
     */
    Page<ResourceDTO> queryResources(ResourceQueryDTO queryDTO);

    /**
     * 下载资源
     */
    org.springframework.core.io.Resource downloadResource(String id);

    /**
     * 获取资源下载URL
     */
    String getDownloadUrl(String id, String currentUser);

    /**
     * 增加下载次数
     */
    void incrementDownloadCount(String id);

    /**
     * 根据上传者获取资源列表
     */
    List<ResourceDTO> getResourcesByUploader(String uploader);

    /**
     * 获取最近上传的资源
     */
    List<ResourceDTO> getRecentlyUploadedResources(int limit);

    /**
     * 获取热门资源
     */
    List<ResourceDTO> getPopularResources(int limit);

    /**
     * 搜索资源
     */
    List<ResourceDTO> searchResources(String keyword);
}

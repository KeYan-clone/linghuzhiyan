package org.linghu.resource.repository;

import org.linghu.resource.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资源仓储接口
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

    /**
     * 根据实验ID查找资源
     */
    List<Resource> findByExperimentId(String experimentId);

    /**
     * 根据实验ID和资源类型查找资源
     */
    List<Resource> findByExperimentIdAndResourceType(String experimentId, Resource.ResourceType resourceType);

    /**
     * 根据资源类型查找资源
     */
    List<Resource> findByResourceType(Resource.ResourceType resourceType);

    /**
     * 根据上传者查找资源
     */
    List<Resource> findByUploader(String uploader);

    /**
     * 根据上传者和实验ID查找资源
     */
    List<Resource> findByUploaderAndExperimentId(String uploader, String experimentId);

    /**
     * 根据资源路径查找资源
     */
    List<Resource> findByResourcePath(String resourcePath);

    /**
     * 查找公开资源
     */
    List<Resource> findByIsPublicTrue();

    /**
     * 根据实验ID查找公开资源
     */
    List<Resource> findByExperimentIdAndIsPublicTrue(String experimentId);

    /**
     * 分页查找公开资源
     */
    Page<Resource> findByIsPublicTrue(Pageable pageable);

    /**
     * 根据文件名模糊查询
     */
    @Query("SELECT r FROM Resource r WHERE r.fileName LIKE %:fileName%")
    List<Resource> findByFileNameContaining(@Param("fileName") String fileName);

    /**
     * 根据描述模糊查询
     */
    @Query("SELECT r FROM Resource r WHERE r.description LIKE %:description%")
    List<Resource> findByDescriptionContaining(@Param("description") String description);

    /**
     * 根据实验ID和文件名查询
     */
    @Query("SELECT r FROM Resource r WHERE r.experimentId = :experimentId AND r.fileName LIKE %:fileName%")
    List<Resource> findByExperimentIdAndFileNameContaining(@Param("experimentId") String experimentId, 
                                                           @Param("fileName") String fileName);

    /**
     * 统计某个实验的资源数量
     */
    long countByExperimentId(String experimentId);

    /**
     * 统计某个用户上传的资源数量
     */
    long countByUploader(String uploader);

    /**
     * 根据MIME类型查找资源
     */
    List<Resource> findByMimeTypeStartingWith(String mimeTypePrefix);

    /**
     * 查找最近上传的资源
     */
    @Query("SELECT r FROM Resource r ORDER BY r.createdAt DESC")
    List<Resource> findRecentlyUploaded(Pageable pageable);

    /**
     * 查找热门资源（按下载次数排序）
     */
    @Query("SELECT r FROM Resource r WHERE r.isPublic = true ORDER BY r.downloadCount DESC")
    List<Resource> findPopularResources(Pageable pageable);
}

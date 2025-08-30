package org.linghu.experiment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.experiment.client.UserServiceClient;
import org.linghu.experiment.domain.Experiment;
import org.linghu.experiment.dto.ExperimentDTO;
import org.linghu.experiment.dto.ExperimentRequestDTO;
import org.linghu.experiment.dto.UserDTO;
import org.linghu.experiment.repository.ExperimentRepository;
import org.linghu.experiment.repository.ExperimentTaskRepository;
import org.linghu.experiment.service.ExperimentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 实验管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ExperimentTaskRepository taskRepository;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public ExperimentDTO createExperiment(ExperimentRequestDTO requestDTO, String creatorUsername) {
        UserDTO creator = userServiceClient.getUserByUsername(creatorUsername);
        if (creator == null) {
            throw new RuntimeException("用户不存在");
        }
        
        log.info("创建实验: {}, 创建者: {}", requestDTO.getName(), creatorUsername);
        
        Experiment experiment = Experiment.builder()
                .id(UUID.randomUUID().toString())
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : Experiment.ExperimentStatus.DRAFT)
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .creatorId(creator.getId())
                .build();

        Experiment savedExperiment = experimentRepository.save(experiment);

        return convertToDTO(savedExperiment);
    }

    @Override
    public Page<ExperimentDTO> getAllExperiments(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Experiment> experiments = experimentRepository.findAll(pageable);

        return experiments.map(experiment -> {
            return convertToDTO(experiment);
        });
    }

    @Override
    public ExperimentDTO getExperimentById(String id) {
        Experiment experiment = experimentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验不存在"));

        return convertToDTO(experiment);
    }

    @Override
    @Transactional
    public ExperimentDTO updateExperiment(String id, ExperimentRequestDTO requestDTO, String username) {
        log.info("更新实验: {}, 用户: {}", id, username);
        
        Experiment experiment = experimentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验不存在"));

        UserDTO user = userServiceClient.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查权限（仅创建者可以更新）
        if (!experiment.getCreatorId().equals(user.getId())) {
            throw new AccessDeniedException("无权更新此实验");
        }
        experiment.setName(requestDTO.getName());
        experiment.setDescription(requestDTO.getDescription());
        experiment.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : experiment.getStatus());
        experiment.setStartTime(requestDTO.getStartTime());
        experiment.setEndTime(requestDTO.getEndTime());

        Experiment updatedExperiment = experimentRepository.save(experiment);

        return convertToDTO(updatedExperiment);
    }

    @Override
    @Transactional
    public void deleteExperiment(String id) {
        if (!experimentRepository.existsById(id)) {
            throw new RuntimeException("实验不存在");
        }
        experimentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ExperimentDTO publishExperiment(String id) {
        Experiment experiment = experimentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验不存在"));

        String username = getCurrentUsernameFromSecurityContext();
        if (username == null || "anonymousUser".equals(username)) {
            throw new AccessDeniedException("未认证或权限不足：无权发布此实验");
        }
        UserDTO user = userServiceClient.getUserByUsername(username);
        if (user==null||!experiment.getCreatorId().equals(user.getId())) {
            throw new AccessDeniedException("权限不足：无权取消发布此实验");
        }

        experiment.setStatus(Experiment.ExperimentStatus.PUBLISHED);
        Experiment publishedExperiment = experimentRepository.save(experiment);

    

        return convertToDTO(publishedExperiment);
    }

    @Override
    @Transactional
    public ExperimentDTO unpublishExperiment(String id) {
        Experiment experiment = experimentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("实验不存在"));
        String username =getCurrentUsernameFromSecurityContext();
        if (username == null || "anonymousUser".equals(username)) {
            throw new AccessDeniedException("未认证或权限不足：无权取消发布此实验");
        }
        UserDTO user = userServiceClient.getUserByUsername(username);
        if (user==null||!experiment.getCreatorId().equals(user.getId())) {
            throw new AccessDeniedException("权限不足：无权取消发布此实验");
        }
        experiment.setStatus(Experiment.ExperimentStatus.DRAFT);
        Experiment unpublishedExperiment = experimentRepository.save(experiment);

        return convertToDTO(unpublishedExperiment);
    }

    /**
     * 将实验实体转换为DTO
     * 
     * @param experiment  实验实体
     * @return 实验DTO
     */
    private ExperimentDTO convertToDTO(Experiment experiment) {
        ExperimentDTO dto = new ExperimentDTO();
        dto.setId(experiment.getId());
        dto.setCreator_Id(experiment.getCreatorId());
        dto.setName(experiment.getName());
        dto.setDescription(experiment.getDescription());
        dto.setStatus(experiment.getStatus());
        dto.setStartTime(experiment.getStartTime());
        dto.setEndTime(experiment.getEndTime());
        return dto;
    }

    /**
     * 获取当前认证用户的用户名
     *
     * @return 当前用户名，若未认证则返回null
     */
    protected String getCurrentUsernameFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
}

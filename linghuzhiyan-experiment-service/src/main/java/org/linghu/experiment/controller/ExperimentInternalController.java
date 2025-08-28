package org.linghu.experiment.controller;

import org.linghu.experiment.dto.ExperimentDTO;
import org.linghu.experiment.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 实验服务内部API控制器 - 用于微服务间通信
 * 
 * 该控制器提供内部接口，供其他微服务调用
 * 不需要身份验证，仅用于内部服务间调用
 */
@RestController
@RequestMapping("/api/internal/experiments")
public class ExperimentInternalController {

    private final ExperimentService experimentService;

    @Autowired
    public ExperimentInternalController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    /**
     * 根据ID获取实验信息 - 内部接口
     * 
     * @param experimentId 实验ID
     * @return 实验信息
     */
    @GetMapping("/{experimentId}")
    public ExperimentDTO getExperimentById(@PathVariable String experimentId) {
        return experimentService.getExperimentById(experimentId);
    }

    /**
     * 验证实验是否存在 - 内部接口
     * 
     * @param experimentId 实验ID
     * @return 是否存在
     */
    @GetMapping("/{experimentId}/exists")
    public Boolean experimentExists(@PathVariable String experimentId) {
        try {
            ExperimentDTO experiment = experimentService.getExperimentById(experimentId);
            return experiment != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 批量验证实验是否存在 - 内部接口
     * 
     * @param experimentIds 实验ID列表
     * @return 验证结果映射
     */
    @PostMapping("/batch-exists")
    public java.util.Map<String, Boolean> batchExperimentExists(@RequestBody java.util.List<String> experimentIds) {
        java.util.Map<String, Boolean> result = new java.util.HashMap<>();
        for (String experimentId : experimentIds) {
            try {
                ExperimentDTO experiment = experimentService.getExperimentById(experimentId);
                result.put(experimentId, experiment != null);
            } catch (Exception e) {
                result.put(experimentId, false);
            }
        }
        return result;
    }

    /**
     * 获取实验的基本信息 - 内部接口
     * 
     * @param experimentId 实验ID
     * @return 基本信息
     */
    @GetMapping("/{experimentId}/basic")
    public ExperimentDTO getExperimentBasicInfo(@PathVariable String experimentId) {
        return experimentService.getExperimentById(experimentId);
    }
}

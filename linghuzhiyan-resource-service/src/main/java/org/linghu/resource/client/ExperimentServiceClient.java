package org.linghu.resource.client;

import org.linghu.resource.dto.ExperimentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 实验服务Feign客户端
 */
@FeignClient(name = "linghuzhiyan-experiment-service", path = "/api/internal/experiments")
public interface ExperimentServiceClient {

    /**
     * 根据ID获取实验信息 - 内部接口
     * 
     * @param experimentId 实验ID
     * @return 实验信息
     */
    @GetMapping("/{experimentId}")
    ExperimentDTO getExperimentById(@PathVariable("experimentId") String experimentId);

    /**
     * 验证实验是否存在 - 内部接口
     * 
     * @param experimentId 实验ID
     * @return 是否存在
     */
    @GetMapping("/{experimentId}/exists")
    Boolean experimentExists(@PathVariable("experimentId") String experimentId);
}

package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;
import com.xx.jaseatschoicejava.dto.AiPreviousConfigSummary;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;

import java.util.List;

/**
 * AI 运行时配置服务。
 */
public interface AiRuntimeConfigService {

    AiRuntimeConfigSnapshot getCurrentSnapshot();

    AiConnectivityResult testCurrentConnectivity();

    AiConnectivityResult testCandidateConnectivity(AiModelCandidateDTO candidate);

    List<AiModelOption> discoverModels(AiModelCandidateDTO candidate);

    AiRuntimeConfigSnapshot saveAndActivate(AiModelCandidateDTO candidate);

    AiRuntimeConfigSnapshot saveAndActivate(AiModelCandidateDTO candidate, String operator);

    AiPreviousConfigSummary getPreviousConfigSummary();

    AiRuntimeConfigSnapshot rollbackToPrevious(String operator);

    /**
     * 手动重试公告索引重建，返回包含当前 RAG 状态的最新快照。
     */
    AiRuntimeConfigSnapshot rebuildEmbeddingIndex(String operator);

    /**
     * 运行时发现视觉模型实际拒绝图片输入时，把当前配置的视觉能力标记为「未验证」。
     *
     * <p>调用后 capabilities 的 vision 立即变为 false，识别入口不再宣称已开放，
     * 需要管理员重新保存配置并通过图片验证才能恢复。
     */
    void markVisionUnverified(String reason);
}

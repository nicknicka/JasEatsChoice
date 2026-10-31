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
}

package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;

/**
 * 管理端只读 AI 运行时配置服务。
 */
public interface AiRuntimeConfigService {

    AiRuntimeConfigSnapshot getCurrentSnapshot();

    AiConnectivityResult testCurrentConnectivity();
}

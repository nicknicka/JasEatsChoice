package com.xx.jaseatschoicejava.service;

import java.util.List;

import com.xx.jaseatschoicejava.rag.AnnouncementEmbeddingConfig;

/**
 * 公告 Embedding 运行时边界：生效配置的兜底合并、候选配置校验、切换生效与密钥状态。
 */
public interface AnnouncementEmbeddingRuntime {

    /**
     * Embedding 独立密钥槽位的脱敏状态，不携带明文。
     */
    record KeyStatus(boolean configured, String fingerprint, String updatedAt) {
    }

    /**
     * 把持久化配置与环境变量兜底合并成生效配置；持久化缺项（首次上线、历史配置）回落到环境变量。
     */
    AnnouncementEmbeddingConfig effective(AnnouncementEmbeddingConfig persisted);

    /**
     * 校验候选配置：连通性 + 一次真实 Embedding 调用，并比对现有 collection 维度。
     * 不通过时抛 BusinessException，调用方不得切换配置。
     */
    void validate(AnnouncementEmbeddingConfig candidate, String candidateKey);

    /**
     * 让公告检索侧立即使用该配置。
     */
    void activate(AnnouncementEmbeddingConfig config);

    /**
     * 后端已实现的 Embedding provider 列表。
     */
    List<String> providers();

    /**
     * 当前生效的 Embedding 密钥状态。
     */
    KeyStatus keyStatus();
}

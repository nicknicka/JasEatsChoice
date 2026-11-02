package com.xx.jaseatschoicejava.service;

import java.util.List;

import com.xx.jaseatschoicejava.rag.AnnouncementEmbeddingConfig;
import com.xx.jaseatschoicejava.rag.AnnouncementRagStatus;

/**
 * 公告 Embedding 运行时边界：生效配置的兜底合并、候选配置校验、切换生效、索引重建与状态。
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
     * 启动/重启恢复：把该配置设为当前生效值，不触发重建（索引本来就是按它建的）。
     */
    void activate(AnnouncementEmbeddingConfig config);

    /**
     * 管理员切换或回滚配置：生效后按需触发索引重建。
     *
     * @return 是否因此触发了重建
     */
    boolean switchConfig(AnnouncementEmbeddingConfig config);

    /**
     * 手动重试一次公告索引重建（异步；已有重建在跑时忽略）。
     */
    void rebuildIndex();

    /**
     * 当前公告 RAG 状态，含「重建中」与失败原因。
     */
    AnnouncementRagStatus status();

    /**
     * 后端已实现的 Embedding provider 列表。
     */
    List<String> providers();

    /**
     * 当前生效的 Embedding 密钥状态。
     */
    KeyStatus keyStatus();
}

package com.xx.jaseatschoicejava.rag;

/**
 * 公告 Embedding 的生效配置。
 *
 * 服务地址与密钥不改变向量空间，换地址/密钥属于零等待路径；模型与维度改变向量空间，
 * 换模型需要重建索引，维度变化需要新建 collection。
 */
public record AnnouncementEmbeddingConfig(String provider, String model, int dimensions, String baseUrl) {
}

package com.xx.jaseatschoicejava.rag;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;

public class QdrantCollectionInitializer {

    private static final Map<String, Collections.PayloadSchemaType> PAYLOAD_INDEXES = Map.ofEntries(
        Map.entry("sourceType", Collections.PayloadSchemaType.Keyword),
        Map.entry("merchantId", Collections.PayloadSchemaType.Keyword),
        Map.entry("targetAudience", Collections.PayloadSchemaType.Keyword),
        Map.entry("audienceTags", Collections.PayloadSchemaType.Keyword),
        Map.entry("status", Collections.PayloadSchemaType.Keyword),
        Map.entry("contentId", Collections.PayloadSchemaType.Keyword),
        Map.entry("startAt", Collections.PayloadSchemaType.Datetime),
        Map.entry("endAt", Collections.PayloadSchemaType.Datetime));

    private final QdrantClient client;
    private final AnnouncementRagProperties properties;

    public QdrantCollectionInitializer(QdrantClient client, AnnouncementRagProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public void initialize() throws Exception {
        int dimensions = properties.getEmbedding().getDimensions();
        if (dimensions <= 0) {
            throw new IllegalStateException("公告 RAG embedding 维度必须大于 0，当前为 " + dimensions);
        }
        String collectionName = properties.getQdrant().getCollection();
        if (collectionName == null || collectionName.isBlank()) {
            throw new IllegalStateException("公告 RAG collection 名称不能为空");
        }

        Collections.CollectionInfo info = getCollectionInfo(collectionName);
        if (info == null) {
            client.createCollectionAsync(collectionName, Collections.VectorParams.newBuilder()
                    .setSize(dimensions)
                    .setDistance(Collections.Distance.Cosine)
                    .build())
                .get(properties.getQdrant().getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            info = getCollectionInfo(collectionName);
        }
        else {
            int existingDimensions = existingDimension(info);
            if (existingDimensions != dimensions) {
                throw new IllegalStateException("公告 RAG collection " + collectionName
                    + " 向量维度为 " + existingDimensions + "，与配置的 embedding 维度 " + dimensions + " 不兼容");
            }
        }

        createPayloadIndexes(collectionName);
    }

    private Collections.CollectionInfo getCollectionInfo(String collectionName) throws Exception {
        try {
            return client.getCollectionInfoAsync(collectionName)
                .get(properties.getQdrant().getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (Exception exception) {
            if (isNotFound(exception)) {
                return null;
            }
            throw exception;
        }
    }

    private int existingDimension(Collections.CollectionInfo info) {
        Collections.VectorsConfig vectorsConfig = info.getConfig().getParams().getVectorsConfig();
        if (vectorsConfig.hasParams()) {
            return (int) vectorsConfig.getParams().getSize();
        }
        if (vectorsConfig.hasParamsMap() && vectorsConfig.getParamsMap().containsMap("")) {
            return (int) vectorsConfig.getParamsMap().getMapOrThrow("").getSize();
        }
        throw new IllegalStateException("公告 RAG collection " + properties.getQdrant().getCollection()
            + " 缺少默认向量维度配置");
    }

    private void createPayloadIndexes(String collectionName) throws Exception {
        for (Map.Entry<String, Collections.PayloadSchemaType> index : PAYLOAD_INDEXES.entrySet()) {
            client.createPayloadIndexAsync(collectionName, index.getKey(), index.getValue(), null,
                    true, null, properties.getQdrant().getTimeout())
                .get(properties.getQdrant().getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private boolean isNotFound(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof StatusRuntimeException statusException
                && Status.fromThrowable(statusException).getCode() == Status.Code.NOT_FOUND) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
}

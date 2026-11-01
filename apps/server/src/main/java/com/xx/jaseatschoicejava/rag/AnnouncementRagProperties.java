package com.xx.jaseatschoicejava.rag;

import java.time.Duration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "announcement-rag")
public class AnnouncementRagProperties {

    private boolean enabled = true;
    private Qdrant qdrant = new Qdrant();
    private Embedding embedding = new Embedding();

    @Data
    public static class Qdrant {
        private String host = "localhost";
        private int port = 6334;
        private String collection = "jaseats_knowledge_v1";
        private Duration timeout = Duration.ofSeconds(5);
    }

    @Data
    public static class Embedding {
        private String provider = "zhipu";
        private String model = "embedding-3";
        private int dimensions = 2048;
        private String baseUrl = "";
    }
}

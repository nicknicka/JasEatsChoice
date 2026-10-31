package com.xx.jaseatschoicejava.rag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xx.jaseatschoicejava.service.AnnouncementService;

@Configuration
public class RagKnowledgeConfig {

    @Bean
    public RagKnowledgeService ragKnowledgeService(
        AnnouncementService announcementService,
        AnnouncementRagInfrastructure announcementRagInfrastructure) {
        return new RagKnowledgeService(announcementService, announcementRagInfrastructure);
    }
}

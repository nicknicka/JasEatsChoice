package com.xx.jaseatschoicejava.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.xx.jaseatschoicejava.rag.RagKnowledgeService;

class AdminAnnouncementRagControllerTest {

    private RagKnowledgeService ragKnowledgeService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ragKnowledgeService = Mockito.mock(RagKnowledgeService.class);
        AdminAnnouncementController controller = new AdminAnnouncementController();
        ReflectionTestUtils.setField(controller, "ragKnowledgeService", ragKnowledgeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void rebuildsOneAnnouncement() throws Exception {
        mockMvc.perform(post("/admin/announcements/announcement-1/rag/rebuild"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(ragKnowledgeService).rebuildAnnouncement("announcement-1");
    }

    @Test
    void rebuildsAllAnnouncements() throws Exception {
        when(ragKnowledgeService.rebuildAllAnnouncements()).thenReturn(2);

        mockMvc.perform(post("/admin/announcements/rag/rebuild"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.rebuildCount").value(2));

        verify(ragKnowledgeService).rebuildAllAnnouncements();
    }
}

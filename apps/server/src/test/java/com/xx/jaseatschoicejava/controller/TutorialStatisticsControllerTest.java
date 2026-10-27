package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.service.TutorialService;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 教程统计控制器测试
 */
public class TutorialStatisticsControllerTest {

    @Test
    public void getOverview_shouldReturnStatisticsFromService() {
        TutorialService tutorialService = mock(TutorialService.class);
        TutorialStatisticsController controller = new TutorialStatisticsController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "tutorialService", tutorialService);

        Map<String, Object> statistics = Map.of(
                "totalTutorials", 10L,
                "publishedTutorials", 7L,
                "pendingReview", 2L,
                "totalViews", 1234L,
                "totalFavorites", 56L,
                "averageRating", 4.5
        );
        when(tutorialService.getOverviewStatistics()).thenReturn(statistics);

        Map<String, Object> response = controller.getOverview().getBody();

        assertEquals(statistics, response);
        verify(tutorialService).getOverviewStatistics();
    }
}

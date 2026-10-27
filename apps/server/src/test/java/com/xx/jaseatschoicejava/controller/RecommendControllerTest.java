package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.mapper.RejectRecommendationMapper;
import com.xx.jaseatschoicejava.service.RecommendationService;
import com.xx.jaseatschoicejava.service.UserBehaviorService;
import com.xx.jaseatschoicejava.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class RecommendControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private UserBehaviorService userBehaviorService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private RejectRecommendationMapper rejectRecommendationMapper;

    @InjectMocks
    private RecommendController controller;

    @Test
    void recordFeedback_whenServiceFails_shouldReturnFailure() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "user-1");
        params.put("dishId", "dish-1");
        params.put("recommendationId", "rec-1");
        params.put("isClicked", true);
        params.put("isOrdered", false);

        doThrow(new IllegalStateException("db down"))
                .when(recommendationService)
                .recordFeedback("user-1", "dish-1", "rec-1", true, false);

        ResponseResult<?> response = controller.recordFeedback(params);

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
    }
}

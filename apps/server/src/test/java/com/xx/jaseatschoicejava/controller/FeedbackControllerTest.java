package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.service.FeedbackService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import com.xx.jaseatschoicejava.vo.FeedbackVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FeedbackController controller;

    @Test
    void create_shouldUseVerifiedUserIdFromJwtAttribute() {
        when(request.getAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE)).thenReturn("user-42");
        Feedback saved = new Feedback();
        saved.setFeedbackId("fb-1");
        saved.setUserId("user-42");
        saved.setType("bug");
        saved.setContent("问题");
        saved.setStatus("pending");
        when(feedbackService.create(org.mockito.ArgumentMatchers.eq("user-42"),
                org.mockito.ArgumentMatchers.any(FeedbackCreateDTO.class))).thenReturn(saved);

        ResponseResult<FeedbackVO> response = controller.create(new FeedbackCreateDTO(), request);

        assertEquals(Boolean.TRUE, response.getSuccess());
        assertEquals("fb-1", response.getData().getFeedbackId());
        verify(feedbackService).create(org.mockito.ArgumentMatchers.eq("user-42"),
                org.mockito.ArgumentMatchers.any(FeedbackCreateDTO.class));
    }

    @Test
    void create_whenNotAuthenticated_shouldReturn401() {
        when(request.getAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.create(new FeedbackCreateDTO(), request));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void detail_shouldPassVerifiedUserIdToService() {
        when(request.getAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE)).thenReturn("user-42");
        Feedback mine = new Feedback();
        mine.setFeedbackId("fb-9");
        mine.setUserId("user-42");
        mine.setType("ui");
        mine.setContent("错位");
        mine.setStatus("pending");
        when(feedbackService.getOwn("user-42", "fb-9")).thenReturn(mine);

        ResponseResult<FeedbackVO> response = controller.detail("fb-9", request);

        assertEquals("fb-9", response.getData().getFeedbackId());
        verify(feedbackService).getOwn("user-42", "fb-9");
    }
}

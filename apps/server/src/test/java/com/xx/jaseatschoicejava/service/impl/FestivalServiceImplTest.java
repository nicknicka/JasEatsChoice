package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.dto.RecommendFeedbackDTO;
import com.xx.jaseatschoicejava.entity.Festival;
import com.xx.jaseatschoicejava.entity.FestivalDishRecommend;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FestivalDishRecommendMapper;
import com.xx.jaseatschoicejava.mapper.FestivalMapper;
import com.xx.jaseatschoicejava.mapper.UserCustomEventMapper;
import com.xx.jaseatschoicejava.vo.FestivalRecommendVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FestivalServiceImplTest {

    @Mock
    private FestivalMapper festivalMapper;

    @Mock
    private FestivalDishRecommendMapper festivalDishRecommendMapper;

    @Mock
    private UserCustomEventMapper userCustomEventMapper;

    @InjectMocks
    private FestivalServiceImpl festivalService;

    @Test
    public void submitRecommendFeedback_shouldIncrementPersistedRecord() {
        RecommendFeedbackDTO dto = new RecommendFeedbackDTO();
        dto.setRecommendHistoryId("recommend-1");
        dto.setIsClicked(true);
        dto.setIsOrdered(true);

        when(festivalDishRecommendMapper.incrementFeedbackCounts("recommend-1", 1, 1))
            .thenReturn(1);

        assertTrue(festivalService.submitRecommendFeedback(dto, "user-1"));
        verify(festivalDishRecommendMapper).incrementFeedbackCounts("recommend-1", 1, 1);
    }

    @Test
    public void submitRecommendFeedback_whenRecordMissing_shouldFail() {
        RecommendFeedbackDTO dto = new RecommendFeedbackDTO();
        dto.setRecommendHistoryId("missing");
        dto.setIsClicked(true);

        when(festivalDishRecommendMapper.incrementFeedbackCounts("missing", 1, 0))
            .thenReturn(0);

        assertThrows(BusinessException.class,
            () -> festivalService.submitRecommendFeedback(dto, "user-1"));
    }

    @Test
    public void getActiveFestivalRecommends_shouldReturnPersistedRecommendId() {
        Festival festival = new Festival();
        festival.setId("festival-1");

        FestivalDishRecommend recommend = new FestivalDishRecommend();
        recommend.setId("recommend-1");
        recommend.setFestivalId("festival-1");
        recommend.setDishId("dish-1");

        when(festivalMapper.selectActiveFestivals()).thenReturn(List.of(festival));
        when(festivalDishRecommendMapper.selectByFestivalIdWithDish("festival-1"))
            .thenReturn(List.of(recommend));

        List<FestivalRecommendVO> result =
            festivalService.getActiveFestivalRecommends("user-1");

        assertEquals("recommend-1",
            result.get(0).getRecommendDishes().get(0).getRecommendHistoryId());
    }

    @Test
    public void recordRecommend_shouldReturnPersistedRecommendId() {
        FestivalDishRecommend recommend = new FestivalDishRecommend();
        recommend.setId("recommend-1");

        when(festivalDishRecommendMapper.selectByFestivalAndDish("festival-1", "dish-1"))
            .thenReturn(recommend);

        assertEquals("recommend-1",
            festivalService.recordRecommend("user-1", "festival-1", "dish-1", "MAIN"));
    }
}

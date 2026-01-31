package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.FestivalDishRecommendDTO;
import com.xx.jaseatschoicejava.dto.RecommendFeedbackDTO;
import com.xx.jaseatschoicejava.dto.UserCustomEventCreateDTO;
import com.xx.jaseatschoicejava.service.FestivalService;
import com.xx.jaseatschoicejava.vo.FestivalRecommendVO;
import com.xx.jaseatschoicejava.vo.UserEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 节日推荐控制器
 *
 * @author Claude
 * @since 2025-01-31
 */
@RestController
@RequestMapping("/v1/festival")
public class FestivalController {

    private static final Logger log = LoggerFactory.getLogger(FestivalController.class);

    @Autowired
    private FestivalService festivalService;

    /**
     * 获取当前生效的节日推荐
     *
     * @param request HTTP请求
     * @return 节日推荐列表
     */
    @GetMapping("/active")
    public ResponseResult<List<FestivalRecommendVO>> getActiveFestivals(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<FestivalRecommendVO> recommends = festivalService.getActiveFestivalRecommends(userId);
        return ResponseResult.success(recommends);
    }

    /**
     * 获取即将到来的节日推荐
     *
     * @param days 天数
     * @param request HTTP请求
     * @return 节日推荐列表
     */
    @GetMapping("/upcoming")
    public ResponseResult<List<FestivalRecommendVO>> getUpcomingFestivals(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<FestivalRecommendVO> recommends = festivalService.getUpcomingFestivalRecommends(userId, days);
        return ResponseResult.success(recommends);
    }

    /**
     * 根据类型获取节日推荐
     *
     * @param festivalType 节日类型
     * @param request HTTP请求
     * @return 节日推荐列表
     */
    @GetMapping("/type/{festivalType}")
    public ResponseResult<List<FestivalRecommendVO>> getFestivalsByType(
            @PathVariable String festivalType,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<FestivalRecommendVO> recommends = festivalService.getFestivalRecommendsByType(userId, festivalType);
        return ResponseResult.success(recommends);
    }

    /**
     * 获取首页推荐
     *
     * @param limit 限制数量
     * @param request HTTP请求
     * @return 节日推荐列表
     */
    @GetMapping("/homepage")
    public ResponseResult<List<FestivalRecommendVO>> getHomepageRecommends(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<FestivalRecommendVO> recommends = festivalService.getHomepageRecommends(userId, limit);
        return ResponseResult.success(recommends);
    }

    /**
     * 添加节日推荐菜品（管理员/商家）
     *
     * @param dto 推荐DTO
     * @return 操作结果
     */
    @PostMapping("/recommend")
    public ResponseResult<Boolean> addFestivalDishRecommend(@RequestBody FestivalDishRecommendDTO dto) {
        boolean success = festivalService.addFestivalDishRecommend(dto);
        return ResponseResult.success(success);
    }

    /**
     * 创建用户自定义事件
     *
     * @param dto 创建DTO
     * @param request HTTP请求
     * @return 事件ID
     */
    @PostMapping("/custom-event")
    public ResponseResult<String> createCustomEvent(
            @RequestBody UserCustomEventCreateDTO dto,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        String eventId = festivalService.createUserCustomEvent(dto, userId);
        return ResponseResult.success(eventId);
    }

    /**
     * 获取用户自定义事件列表
     *
     * @param request HTTP请求
     * @return 事件列表
     */
    @GetMapping("/custom-events")
    public ResponseResult<List<UserEventVO>> getCustomEvents(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<UserEventVO> events = festivalService.getUserCustomEvents(userId);
        return ResponseResult.success(events);
    }

    /**
     * 获取用户即将到来的事件
     *
     * @param days 天数
     * @param request HTTP请求
     * @return 事件列表
     */
    @GetMapping("/custom-events/upcoming")
    public ResponseResult<List<UserEventVO>> getUpcomingEvents(
            @RequestParam(defaultValue = "30") int days,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        List<UserEventVO> events = festivalService.getUpcomingUserEvents(userId, days);
        return ResponseResult.success(events);
    }

    /**
     * 提交推荐反馈
     *
     * @param dto 反馈DTO
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/feedback")
    public ResponseResult<Boolean> submitFeedback(
            @RequestBody RecommendFeedbackDTO dto,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = "test_user";
        }

        boolean success = festivalService.submitRecommendFeedback(dto, userId);
        return ResponseResult.success(success);
    }
}

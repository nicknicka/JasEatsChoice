package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.FestivalDishRecommendDTO;
import com.xx.jaseatschoicejava.dto.RecommendFeedbackDTO;
import com.xx.jaseatschoicejava.dto.UserCustomEventCreateDTO;
import com.xx.jaseatschoicejava.vo.FestivalRecommendVO;
import com.xx.jaseatschoicejava.vo.UserEventVO;

import java.util.List;

/**
 * 节日推荐服务接口
 *
 * @author Claude
 * @since 2025-01-31
 */
public interface FestivalService {

    /**
     * 获取当前生效的节日推荐列表
     *
     * @param userId 用户ID
     * @return 节日推荐列表
     */
    List<FestivalRecommendVO> getActiveFestivalRecommends(String userId);

    /**
     * 获取即将到来的节日推荐（指定天数内）
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 节日推荐列表
     */
    List<FestivalRecommendVO> getUpcomingFestivalRecommends(String userId, int days);

    /**
     * 根据类型获取节日推荐
     *
     * @param userId 用户ID
     * @param festivalType 节日类型
     * @return 节日推荐列表
     */
    List<FestivalRecommendVO> getFestivalRecommendsByType(String userId, String festivalType);

    /**
     * 获取首页推荐菜品
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 节日推荐列表
     */
    List<FestivalRecommendVO> getHomepageRecommends(String userId, int limit);

    /**
     * 添加节日推荐菜品
     *
     * @param dto 推荐DTO
     * @return 是否成功
     */
    boolean addFestivalDishRecommend(FestivalDishRecommendDTO dto);

    /**
     * 创建用户自定义事件
     *
     * @param dto 创建DTO
     * @param userId 用户ID
     * @return 事件ID
     */
    String createUserCustomEvent(UserCustomEventCreateDTO dto, String userId);

    /**
     * 获取用户自定义事件列表
     *
     * @param userId 用户ID
     * @return 事件列表
     */
    List<UserEventVO> getUserCustomEvents(String userId);

    /**
     * 获取用户即将到来的事件
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 事件列表
     */
    List<UserEventVO> getUpcomingUserEvents(String userId, int days);

    /**
     * 提交推荐反馈
     *
     * @param dto 反馈DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean submitRecommendFeedback(RecommendFeedbackDTO dto, String userId);

    /**
     * 记录推荐展示（用于个性化推荐算法）
     *
     * @param userId 用户ID
     * @param festivalId 节日ID
     * @param dishId 菜品ID
     * @param recommendType 推荐类型
     * @return 推荐记录ID
     */
    String recordRecommend(String userId, String festivalId, String dishId, String recommendType);
}

package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.dto.FestivalDishRecommendDTO;
import com.xx.jaseatschoicejava.dto.RecommendFeedbackDTO;
import com.xx.jaseatschoicejava.dto.UserCustomEventCreateDTO;
import com.xx.jaseatschoicejava.entity.Festival;
import com.xx.jaseatschoicejava.entity.FestivalDishRecommend;
import com.xx.jaseatschoicejava.entity.UserCustomEvent;
import com.xx.jaseatschoicejava.enums.UserEventType;
import com.xx.jaseatschoicejava.mapper.FestivalDishRecommendMapper;
import com.xx.jaseatschoicejava.mapper.FestivalMapper;
import com.xx.jaseatschoicejava.mapper.UserCustomEventMapper;
import com.xx.jaseatschoicejava.service.FestivalService;
import com.xx.jaseatschoicejava.vo.FestivalRecommendVO;
import com.xx.jaseatschoicejava.vo.UserEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 节日推荐服务实现类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Service
public class FestivalServiceImpl implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    @Autowired
    private FestivalMapper festivalMapper;

    @Autowired
    private FestivalDishRecommendMapper festivalDishRecommendMapper;

    @Autowired
    private UserCustomEventMapper userCustomEventMapper;

    @Override
    public List<FestivalRecommendVO> getActiveFestivalRecommends(String userId) {
        List<Festival> festivals = festivalMapper.selectActiveFestivals();
        return buildFestivalRecommendVOs(userId, festivals);
    }

    @Override
    public List<FestivalRecommendVO> getUpcomingFestivalRecommends(String userId, int days) {
        List<Festival> festivals = festivalMapper.selectUpcomingFestivals(days);
        return buildFestivalRecommendVOs(userId, festivals);
    }

    @Override
    public List<FestivalRecommendVO> getFestivalRecommendsByType(String userId, String festivalType) {
        List<Festival> festivals = festivalMapper.selectByType(festivalType);
        return buildFestivalRecommendVOs(userId, festivals);
    }

    @Override
    public List<FestivalRecommendVO> getHomepageRecommends(String userId, int limit) {
        // 获取当前生效和即将到来的节日
        List<Festival> activeFestivals = festivalMapper.selectActiveFestivals();
        List<Festival> upcomingFestivals = festivalMapper.selectUpcomingFestivals(7);

        // 合并去重
        List<String> festivalIds = new ArrayList<>();
        activeFestivals.forEach(f -> {
            if (!festivalIds.contains(f.getId())) {
                festivalIds.add(f.getId());
            }
        });
        upcomingFestivals.forEach(f -> {
            if (!festivalIds.contains(f.getId())) {
                festivalIds.add(f.getId());
            }
        });

        if (festivalIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询首页推荐菜品
        List<FestivalDishRecommend> recommends = festivalDishRecommendMapper.selectHomepageRecommend(
            festivalIds, 0, limit);

        // 按节日分组
        return groupRecommendsByFestival(userId, recommends, activeFestivals, upcomingFestivals);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFestivalDishRecommend(FestivalDishRecommendDTO dto) {
        for (String dishId : dto.getDishIds()) {
            FestivalDishRecommend recommend = new FestivalDishRecommend();
            recommend.setId(UUID.randomUUID().toString().replace("-", ""));
            recommend.setFestivalId(dto.getFestivalId());
            recommend.setDishId(dishId);
            recommend.setRecommendType(dto.getRecommendType());
            recommend.setRecommendReason(dto.getRecommendReason());
            recommend.setPosition(dto.getPosition() != null ? dto.getPosition() : 0);
            recommend.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
            recommend.setClickCount(0);
            recommend.setOrderCount(0);
            recommend.setIsActive(true);
            recommend.setCreateTime(LocalDateTime.now());
            recommend.setUpdateTime(LocalDateTime.now());

            festivalDishRecommendMapper.insert(recommend);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUserCustomEvent(UserCustomEventCreateDTO dto, String userId) {
        UserCustomEvent event = new UserCustomEvent();
        event.setId(UUID.randomUUID().toString().replace("-", ""));
        event.setUserId(userId);
        event.setEventName(dto.getEventName());
        event.setEventType(dto.getEventType());
        event.setEventDate(dto.getEventDate());
        event.setYear(dto.getYear());
        event.setReminderDays(dto.getReminderDays() != null ? dto.getReminderDays() : 3);
        event.setDescription(dto.getDescription());
        // 简化JSON处理，直接存储为字符串
        if (dto.getPreferredDishIds() != null && !dto.getPreferredDishIds().isEmpty()) {
            event.setPreferredDishes(String.join(",", dto.getPreferredDishIds()));
        }
        event.setGuestCount(dto.getGuestCount());
        event.setBudgetPerPerson(dto.getBudgetPerPerson());
        event.setIsActive(true);
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());

        userCustomEventMapper.insert(event);
        return event.getId();
    }

    @Override
    public List<UserEventVO> getUserCustomEvents(String userId) {
        List<UserCustomEvent> events = userCustomEventMapper.selectByUserId(userId);
        return events.stream()
            .map(this::convertToUserEventVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserEventVO> getUpcomingUserEvents(String userId, int days) {
        List<UserCustomEvent> events = userCustomEventMapper.selectUpcomingEvents(userId, days);
        return events.stream()
            .map(this::convertToUserEventVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRecommendFeedback(RecommendFeedbackDTO dto, String userId) {
        log.info("提交推荐反馈: userId={}, recommendHistoryId={}, feedback={}",
            userId, dto.getRecommendHistoryId(), dto.getFeedbackScore());
        // TODO: 实现反馈记录逻辑
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String recordRecommend(String userId, String festivalId, String dishId, String recommendType) {
        log.info("记录推荐: userId={}, festivalId={}, dishId={}, type={}",
            userId, festivalId, dishId, recommendType);
        // TODO: 实现推荐记录逻辑
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 构建节日推荐VO列表
     */
    private List<FestivalRecommendVO> buildFestivalRecommendVOs(String userId, List<Festival> festivals) {
        List<FestivalRecommendVO> result = new ArrayList<>();

        for (Festival festival : festivals) {
            FestivalRecommendVO vo = new FestivalRecommendVO();
            vo.setFestivalId(festival.getId());
            vo.setFestivalName(festival.getFestivalName());
            vo.setFestivalType(festival.getFestivalType());
            vo.setDescription(festival.getDescription());
            vo.setIcon(festival.getIcon());
            vo.setThemeColor(festival.getThemeColor());
            vo.setBackgroundImage(festival.getBackgroundImage());
            vo.setIsCurrent(festival.getIsCurrent());
            vo.setDaysUntilFestival(festival.getDaysUntilFestival());

            // 查询推荐菜品
            List<FestivalDishRecommend> recommends =
                festivalDishRecommendMapper.selectByFestivalIdWithDish(festival.getId());

            // 转换为VO并记录推荐
            List<FestivalRecommendVO.DishRecommendItemVO> dishItems = recommends.stream()
                .map(rec -> {
                    FestivalRecommendVO.DishRecommendItemVO item = new FestivalRecommendVO.DishRecommendItemVO();
                    item.setDishId(rec.getDishId());
                    item.setDishName(rec.getDishName());
                    item.setDishImage(rec.getDishImage());
                    item.setDishPrice(rec.getDishPrice());
                    item.setRecommendType(rec.getRecommendType());
                    item.setRecommendReason(rec.getRecommendReason());
                    item.setPriority(rec.getPriority());

                    // 记录推荐历史
                    String historyId = recordRecommend(userId, festival.getId(),
                        rec.getDishId(), rec.getRecommendType());
                    item.setRecommendHistoryId(historyId);

                    return item;
                })
                .collect(Collectors.toList());

            vo.setRecommendDishes(dishItems);
            result.add(vo);
        }

        return result;
    }

    /**
     * 按节日分组推荐
     */
    private List<FestivalRecommendVO> groupRecommendsByFestival(
            String userId,
            List<FestivalDishRecommend> recommends,
            List<Festival> activeFestivals,
            List<Festival> upcomingFestivals) {

        // 合并所有节日
        List<Festival> allFestivals = new ArrayList<>(activeFestivals);
        for (Festival f : upcomingFestivals) {
            if (!allFestivals.stream().anyMatch(af -> af.getId().equals(f.getId()))) {
                allFestivals.add(f);
            }
        }

        List<FestivalRecommendVO> result = new ArrayList<>();

        for (Festival festival : allFestivals) {
            List<FestivalDishRecommend> festivalRecommends = recommends.stream()
                .filter(r -> r.getFestivalId().equals(festival.getId()))
                .collect(Collectors.toList());

            if (!festivalRecommends.isEmpty()) {
                FestivalRecommendVO vo = new FestivalRecommendVO();
                vo.setFestivalId(festival.getId());
                vo.setFestivalName(festival.getFestivalName());
                vo.setFestivalType(festival.getFestivalType());
                vo.setDescription(festival.getDescription());
                vo.setIcon(festival.getIcon());
                vo.setThemeColor(festival.getThemeColor());
                vo.setIsCurrent(festival.getIsCurrent());
                vo.setDaysUntilFestival(festival.getDaysUntilFestival());

                List<FestivalRecommendVO.DishRecommendItemVO> dishItems = festivalRecommends.stream()
                    .map(rec -> {
                        FestivalRecommendVO.DishRecommendItemVO item = new FestivalRecommendVO.DishRecommendItemVO();
                        item.setDishId(rec.getDishId());
                        item.setDishName(rec.getDishName());
                        item.setDishImage(rec.getDishImage());
                        item.setDishPrice(rec.getDishPrice());
                        item.setRecommendType(rec.getRecommendType());
                        item.setRecommendReason(rec.getRecommendReason());
                        item.setPriority(rec.getPriority());

                        String historyId = recordRecommend(userId, festival.getId(),
                            rec.getDishId(), rec.getRecommendType());
                        item.setRecommendHistoryId(historyId);

                        return item;
                    })
                    .collect(Collectors.toList());

                vo.setRecommendDishes(dishItems);
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 转换为用户事件VO
     */
    private UserEventVO convertToUserEventVO(UserCustomEvent event) {
        UserEventVO vo = new UserEventVO();
        BeanUtils.copyProperties(event, vo);

        // 设置事件类型图标
        UserEventType eventType = UserEventType.getByCode(event.getEventType());
        if (eventType != null) {
            vo.setEventTypeIcon(eventType.getIcon());
        }

        // 计算距离事件天数
        try {
            String eventDate = event.getEventDate();
            int year = event.getYear() != null ? event.getYear() : java.time.Year.now().getValue();
            LocalDateTime eventDateTime = LocalDateTime.of(
                year,
                Integer.parseInt(eventDate.split("-")[0]),
                Integer.parseInt(eventDate.split("-")[1]),
                0, 0
            );
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDateTime);
            vo.setDaysUntilEvent((int) days);
        } catch (Exception e) {
            log.error("计算事件天数失败: eventId={}", event.getId(), e);
            vo.setDaysUntilEvent(null);
        }

        return vo;
    }
}

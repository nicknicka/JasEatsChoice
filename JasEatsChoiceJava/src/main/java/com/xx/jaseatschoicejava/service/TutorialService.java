package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Tutorial;

import java.util.List;

/**
 * 教程服务接口
 */
public interface TutorialService extends IService<Tutorial> {

    // ========== 基础查询 ==========

    /**
     * 获取首页精选教程（已发布且审核通过）
     */
    List<Tutorial> getFeaturedTutorials();

    /**
     * 获取所有已发布的教程
     */
    List<Tutorial> getAllTutorials();

    /**
     * 根据ID获取教程详情
     */
    Tutorial getTutorialDetail(String id);

    /**
     * 分页查询教程
     */
    Page<Tutorial> getTutorialsByPage(int page, int size, String sourceType, String status);

    // ========== 管理员操作 ==========

    /**
     * 管理员创建教程（直接发布）
     */
    Tutorial createByAdmin(Tutorial tutorial);

    /**
     * 获取待审核的教程列表
     */
    Page<Tutorial> getPendingTutorials(int page, int size);

    /**
     * 审核通过
     */
    boolean approveTutorial(String tutorialId, Long reviewerId, String comment, boolean setFeatured);

    /**
     * 审核拒绝
     */
    boolean rejectTutorial(String tutorialId, Long reviewerId, String comment);

    /**
     * 设置/取消精选
     */
    boolean toggleFeatured(String tutorialId, boolean featured);

    // ========== 商家操作 ==========

    /**
     * 商家创建教程（草稿状态）
     */
    Tutorial createByMerchant(Tutorial tutorial);

    /**
     * 商家更新教程
     */
    boolean updateByMerchant(String tutorialId, Tutorial tutorial);

    /**
     * 商家提交审核
     */
    boolean submitForReview(String tutorialId);

    /**
     * 获取商家的教程列表
     */
    Page<Tutorial> getMerchantTutorials(Long merchantId, int page, int size);

    // ========== AI生成 ==========

    /**
     * AI生成教程（草稿状态）
     */
    Tutorial generateByAI(Tutorial tutorial);

    // ========== 统计更新 ==========

    /**
     * 增加浏览次数
     */
    boolean incrementViewCount(String tutorialId);

    /**
     * 更新评分
     */
    boolean updateRating(String tutorialId, double rating);

    /**
     * 增加收藏次数
     */
    boolean incrementFavoriteCount(String tutorialId);
}

package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.mapper.TutorialMapper;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 教程服务实现类
 */
@Service
public class TutorialServiceImpl extends ServiceImpl<TutorialMapper, Tutorial> implements TutorialService {

    @Autowired
    private TutorialMapper tutorialMapper;

    // ========== 基础查询 ==========

    @Override
    public List<Tutorial> getFeaturedTutorials() {
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();
        wrapper.eq("featured", true)
               .eq("status", Tutorial.Status.PUBLISHED.getCode())
               .eq("review_status", Tutorial.ReviewStatus.APPROVED.getCode())
               .orderByDesc("rating")
               .last("LIMIT 10");
        return list(wrapper);
    }

    @Override
    public List<Tutorial> getAllTutorials() {
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Tutorial.Status.PUBLISHED.getCode())
               .eq("review_status", Tutorial.ReviewStatus.APPROVED.getCode())
               .orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    public Tutorial getTutorialDetail(String id) {
        Tutorial tutorial = getById(id);
        if (tutorial != null) {
            // 增加浏览次数
            incrementViewCount(id);
        }
        return tutorial;
    }

    @Override
    public Page<Tutorial> getTutorialsByPage(int page, int size, String sourceType, String status) {
        Page<Tutorial> pageInfo = new Page<>(page, size);
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();

        if (sourceType != null && !sourceType.isEmpty()) {
            wrapper.eq("source_type", sourceType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        } else {
            // 默认只显示已发布且审核通过的
            wrapper.eq("status", Tutorial.Status.PUBLISHED.getCode())
                   .eq("review_status", Tutorial.ReviewStatus.APPROVED.getCode());
        }

        wrapper.orderByDesc("create_time");
        return page(pageInfo, wrapper);
    }

    // ========== 管理员操作 ==========

    @Override
    public List<Tutorial> getAllTutorialsForAdmin() {
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();
        // 管理员可以看到所有状态的教程
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    @Transactional
    public Tutorial createByAdmin(Tutorial tutorial) {
        tutorial.setSourceType(Tutorial.SourceType.ADMIN.getCode());
        tutorial.setAuthorType(Tutorial.AuthorType.ADMIN.getCode());
        tutorial.setStatus(Tutorial.Status.PUBLISHED.getCode());
        tutorial.setReviewStatus(Tutorial.ReviewStatus.APPROVED.getCode());
        tutorial.setOfficial(true);
        tutorial.setCreateTime(new Date());
        tutorial.setUpdateTime(new Date());

        save(tutorial);
        return tutorial;
    }

    @Override
    public Page<Tutorial> getPendingTutorials(int page, int size) {
        Page<Tutorial> pageInfo = new Page<>(page, size);
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();
        wrapper.eq("review_status", Tutorial.ReviewStatus.PENDING.getCode())
               .orderByDesc("create_time");
        return page(pageInfo, wrapper);
    }

    @Override
    @Transactional
    public boolean approveTutorial(String tutorialId, String reviewerId, String comment, boolean setFeatured) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        tutorial.setStatus(Tutorial.Status.PUBLISHED.getCode());
        tutorial.setReviewStatus(Tutorial.ReviewStatus.APPROVED.getCode());
        tutorial.setReviewerId(reviewerId);
        tutorial.setReviewTime(new Date());
        tutorial.setReviewComment(comment);
        tutorial.setFeatured(setFeatured);
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }

    @Override
    @Transactional
    public boolean rejectTutorial(String tutorialId, String reviewerId, String comment) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        tutorial.setStatus(Tutorial.Status.REJECTED.getCode());
        tutorial.setReviewStatus(Tutorial.ReviewStatus.REJECTED.getCode());
        tutorial.setReviewerId(reviewerId);
        tutorial.setReviewTime(new Date());
        tutorial.setReviewComment(comment);
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }

    @Override
    @Transactional
    public boolean toggleFeatured(String tutorialId, boolean featured) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        // 只有已发布且审核通过的教程才能设置为精选
        if (featured && !tutorial.canBeFeatured()) {
            return false;
        }

        tutorial.setFeatured(featured);
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }

    // ========== 商家操作 ==========

    @Override
    @Transactional
    public Tutorial createByMerchant(Tutorial tutorial) {
        tutorial.setSourceType(Tutorial.SourceType.MERCHANT.getCode());
        tutorial.setAuthorType(Tutorial.AuthorType.MERCHANT.getCode());
        tutorial.setStatus(Tutorial.Status.DRAFT.getCode());
        tutorial.setReviewStatus(Tutorial.ReviewStatus.NOT_SUBMITTED.getCode());
        tutorial.setOfficial(false);
        tutorial.setCreateTime(new Date());
        tutorial.setUpdateTime(new Date());

        boolean saved = save(tutorial);
        if (!saved) {
            throw new RuntimeException("保存教程失败");
        }

        // 确保ID已生成
        if (tutorial.getId() == null) {
            throw new RuntimeException("教程ID生成失败");
        }

        return tutorial;
    }

    @Override
    @Transactional
    public boolean updateByMerchant(String tutorialId, Tutorial tutorial) {
        Tutorial existing = getById(tutorialId);
        if (existing == null) {
            return false;
        }

        // 只有草稿或被拒绝的教程才能编辑
        if (!existing.isEditable()) {
            return false;
        }

        tutorial.setId(tutorialId);
        tutorial.setUpdateTime(new Date());
        return updateById(tutorial);
    }

    @Override
    @Transactional
    public boolean submitForReview(String tutorialId) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        tutorial.setStatus(Tutorial.Status.PENDING.getCode());
        tutorial.setReviewStatus(Tutorial.ReviewStatus.PENDING.getCode());
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }

    @Override
    public Page<Tutorial> getMerchantTutorials(String merchantId, int page, int size) {
        Page<Tutorial> pageInfo = new Page<>(page, size);
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();
        wrapper.eq("author_id", merchantId)
               .eq("author_type", Tutorial.AuthorType.MERCHANT.getCode())
               .orderByDesc("create_time");
        return page(pageInfo, wrapper);
    }

    @Override
    public Page<Tutorial> getUserTutorials(String userId, int page, int size) {
        Page<Tutorial> pageInfo = new Page<>(page, size);
        QueryWrapper<Tutorial> wrapper = new QueryWrapper<>();
        wrapper.eq("author_id", userId)
               .eq("author_type", Tutorial.AuthorType.USER.getCode())
               .orderByDesc("create_time");
        return page(pageInfo, wrapper);
    }

    // ========== AI生成 ==========

    @Override
    @Transactional
    public Tutorial generateByAI(Tutorial tutorial) {
        tutorial.setSourceType(Tutorial.SourceType.AI_GENERATED.getCode());
        tutorial.setAuthorType(Tutorial.AuthorType.AI.getCode());
        tutorial.setStatus(Tutorial.Status.DRAFT.getCode());
        tutorial.setReviewStatus(Tutorial.ReviewStatus.NOT_SUBMITTED.getCode());
        tutorial.setOfficial(false);
        tutorial.setAiModelVersion("GPT-4-v2"); // 可以从配置读取
        tutorial.setCreateTime(new Date());
        tutorial.setUpdateTime(new Date());

        save(tutorial);
        return tutorial;
    }

    // ========== 统计更新 ==========

    @Override
    @Transactional
    public boolean incrementViewCount(String tutorialId) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        int currentCount = tutorial.getViewCount() != null ? tutorial.getViewCount() : 0;
        tutorial.setViewCount(currentCount + 1);
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }

    @Override
    @Transactional
    public boolean updateRating(String tutorialId, double rating) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        int currentCount = tutorial.getRatingCount() != null ? tutorial.getRatingCount() : 0;
        double currentRating = tutorial.getRating() != null ? tutorial.getRating().doubleValue() : 0.0;

        // 计算新的平均分
        double newRating = ((currentRating * currentCount) + rating) / (currentCount + 1);

        tutorial.setRating(java.math.BigDecimal.valueOf(newRating));
        tutorial.setRatingCount(currentCount + 1);
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }

    @Override
    @Transactional
    public boolean incrementFavoriteCount(String tutorialId) {
        Tutorial tutorial = getById(tutorialId);
        if (tutorial == null) {
            return false;
        }

        int currentCount = tutorial.getFavoriteCount() != null ? tutorial.getFavoriteCount() : 0;
        tutorial.setFavoriteCount(currentCount + 1);
        tutorial.setUpdateTime(new Date());

        return updateById(tutorial);
    }
}

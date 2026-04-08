package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.entity.ReviewReply;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.ReviewReplyMapper;
import com.xx.jaseatschoicejava.service.MerchantReviewService;
import com.xx.jaseatschoicejava.service.ReviewService;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家评价回复服务实现
 */
@Slf4j
@Service
public class MerchantReviewServiceImpl implements MerchantReviewService {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ReviewReplyMapper reviewReplyMapper;
    private final ChatModel agentModel;

    public MerchantReviewServiceImpl(
            ReviewService reviewService,
            UserService userService,
            ReviewReplyMapper reviewReplyMapper,
            @Qualifier("agentModel") ChatModel agentModel) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.reviewReplyMapper = reviewReplyMapper;
        this.agentModel = agentModel;
    }

    @Override
    public List<PendingReviewDTO> getPendingReviews(String merchantId) {
        // 查询该商家的所有评价
        LambdaQueryWrapper<Review> query = new LambdaQueryWrapper<>();
        query.eq(Review::getMerchantId, merchantId)
             .eq(Review::getStatus, 0)
             .orderByDesc(Review::getCreateTime);
        List<Review> reviews = reviewService.list(query);

        // 查询已回复的评价ID
        LambdaQueryWrapper<ReviewReply> replyQuery = new LambdaQueryWrapper<>();
        replyQuery.eq(ReviewReply::getMerchantId, merchantId);
        List<ReviewReply> replies = reviewReplyMapper.selectList(replyQuery);
        List<String> repliedReviewIds = replies.stream()
                .map(ReviewReply::getReviewId)
                .collect(Collectors.toList());

        // 转换为DTO
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<PendingReviewDTO> result = new ArrayList<>();

        for (Review review : reviews) {
            PendingReviewDTO dto = new PendingReviewDTO();
            dto.setId(review.getId());
            dto.setRating(review.getRating());
            dto.setContent(review.getContent());
            dto.setTime(review.getCreateTime().format(formatter));
            dto.setReplied(repliedReviewIds.contains(review.getId()));

            // 获取用户名称
            User user = userService.getById(review.getUserId());
            dto.setUserName(user != null ? user.getNickname() : "匿名用户");

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<String> generateReplySuggestions(ReviewReplyRequestDTO request) {
        try {
            String prompt = buildReplyPrompt(request);
            String aiResponse = agentModel.chat(prompt);
            return parseReplySuggestions(aiResponse);
        } catch (Exception e) {
            log.error("生成回复建议失败", e);
            return getDefaultSuggestions(request.getRating());
        }
    }

    @Override
    public boolean submitReply(SubmitReplyDTO request) {
        try {
            // 检查是否已回复
            LambdaQueryWrapper<ReviewReply> existQuery = new LambdaQueryWrapper<>();
            existQuery.eq(ReviewReply::getReviewId, request.getReviewId());
            ReviewReply existReply = reviewReplyMapper.selectOne(existQuery);

            if (existReply != null) {
                // 更新回复
                existReply.setContent(request.getContent());
                existReply.setUpdateTime(LocalDateTime.now());
                return reviewReplyMapper.updateById(existReply) > 0;
            } else {
                // 新建回复
                ReviewReply reply = new ReviewReply();
                reply.setReviewId(request.getReviewId());
                reply.setMerchantId(request.getMerchantId());
                reply.setContent(request.getContent());
                reply.setCreateTime(LocalDateTime.now());
                reply.setUpdateTime(LocalDateTime.now());
                reply.setIsAdditional(0);
                return reviewReplyMapper.insert(reply) > 0;
            }
        } catch (Exception e) {
            log.error("提交回复失败", e);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    private String buildReplyPrompt(ReviewReplyRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的餐饮客服，请针对以下顾客评价生成3条不同风格的回复建议。\n\n");
        sb.append("顾客评价信息：\n");
        sb.append(String.format("- 顾客：%s\n", request.getUserName()));
        sb.append(String.format("- 评分：%d星\n", request.getRating()));
        sb.append(String.format("- 评价内容：%s\n\n", request.getReviewContent()));

        if (request.getRating() >= 4) {
            sb.append("回复风格要求：\n");
            sb.append("1. 感谢好评风格：真诚感谢，表达对顾客认可的喜悦\n");
            sb.append("2. 热情邀请风格：感谢支持，邀请再次光临\n");
            sb.append("3. 专业服务风格：感谢信任，强调服务品质\n");
        } else if (request.getRating() == 3) {
            sb.append("回复风格要求：\n");
            sb.append("1. 诚恳致歉风格：承认不足，表达改进决心\n");
            sb.append("2. 积极改进风格：感谢反馈，说明改进措施\n");
            sb.append("3. 服务补救风格：致歉并提供补偿方案\n");
        } else {
            sb.append("回复风格要求：\n");
            sb.append("1. 深刻致歉风格：真诚道歉，表达重视\n");
            sb.append("2. 问题解决风格：承认问题，提供解决方案\n");
            sb.append("3. 服务挽回风格：致歉并请求再给机会\n");
        }

        sb.append("\n请直接返回3条回复，每条回复一行，不要编号和其他解释。每条回复控制在80字以内。");
        return sb.toString();
    }

    private List<String> parseReplySuggestions(String aiResponse) {
        List<String> suggestions = new ArrayList<>();
        String[] lines = aiResponse.split("\n");

        for (String line : lines) {
            line = line.trim();
            // 移除可能的编号前缀
            line = line.replaceFirst("^[1-3][.、)\\s]+", "");
            if (!line.isEmpty() && line.length() > 10) {
                suggestions.add(line);
            }
            if (suggestions.size() >= 3) {
                break;
            }
        }

        // 如果解析失败，返回默认建议
        if (suggestions.isEmpty()) {
            return List.of(
                    "感谢您的反馈！我们会认真对待每一条意见，努力改进服务。",
                    "非常感谢您的评价，您的满意是我们最大的动力，欢迎再次光临！",
                    "感谢您的支持！我们会继续保持优质服务，期待您的再次到来。"
            );
        }

        return suggestions;
    }

    private List<String> getDefaultSuggestions(Integer rating) {
        if (rating >= 4) {
            return List.of(
                    "感谢您的好评！您的满意是我们最大的动力，我们会继续努力为您提供更优质的菜品和服务，期待您的再次光临！",
                    "非常感谢您的认可！我们一直坚持选用新鲜食材，用心做好每一道菜。您的支持是我们前进的动力，欢迎下次再来！",
                    "谢谢您的五星好评！很高兴您喜欢我们的菜品和服务。我们会继续保持，也欢迎您向朋友推荐我们哦！"
            );
        } else if (rating == 3) {
            return List.of(
                    "感谢您的反馈！对于您提到的问题，我们非常重视。我们会加强培训，提升服务效率，希望能给您带来更好的体验。",
                    "非常抱歉给您带来了不好的体验！您提到的问题我们已经记录，会立即改进。期待您再次光临，让我们有机会为您提供更好的服务。",
                    "感谢您的宝贵意见！我们会认真对待每一个问题，努力改进。希望下次能为您提供满意的用餐体验！"
            );
        } else {
            return List.of(
                    "非常抱歉给您带来了不好的体验！您提到的问题我们非常重视，会立即进行整改。希望能有机会再次为您服务，让您看到我们的改变。",
                    "感谢您的反馈，我们深感抱歉！请您联系我们的客服，我们愿意为您提供补偿方案。我们会认真改进，争取下次让您满意。",
                    "非常抱歉让您失望了！您的意见对我们非常重要，我们会认真分析问题并改进。期待您给我们一个弥补的机会！"
            );
        }
    }
}

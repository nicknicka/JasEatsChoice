package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.XhsSessionStore;
import com.xx.jaseatschoicejava.dto.ContentExtractionUpdateDTO;
import com.xx.jaseatschoicejava.dto.ContentSourceCreateDTO;
import com.xx.jaseatschoicejava.dto.XhsSessionBindDTO;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.ContentExtractionService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import com.xx.jaseatschoicejava.vo.ContentExtractionDetailVO;
import com.xx.jaseatschoicejava.vo.ContentSourceVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容提取控制器
 *

 * @since 2025-01-31
 */
@RestController
@RequestMapping("/v1/content-extraction")
public class ContentExtractionController {

    private static final Logger log = LoggerFactory.getLogger(ContentExtractionController.class);

    @Autowired
    private ContentExtractionService contentExtractionService;

    @Autowired
    private XhsSessionStore xhsSessionStore;

    private static final int MAX_XHS_SESSION_LENGTH = 8192;

    /**
     * 添加内容源并开始提取
     *
     * @param dto 创建DTO
     * @param request HTTP请求
     * @return 内容源ID
     */
    @PostMapping("/source")
    public ResponseResult<String> addContentSource(
            @RequestBody ContentSourceCreateDTO dto,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        String sourceId = contentExtractionService.addContentSource(dto, userId);
        return ResponseResult.success(sourceId);
    }

    /**
     * 获取用户的内容源列表
     *
     * @param request HTTP请求
     * @return 内容源列表
     */
    @GetMapping("/sources")
    public ResponseResult<List<ContentSourceVO>> getUserContentSources(HttpServletRequest request) {
        String userId = resolveUserId(request);

        List<ContentSourceVO> sources = contentExtractionService.getUserContentSources(userId);
        return ResponseResult.success(sources);
    }

    /**
     * 获取内容源详情
     *
     * @param sourceId 内容源ID
     * @param request HTTP请求
     * @return 内容源VO
     */
    @GetMapping("/source/{sourceId}")
    public ResponseResult<ContentSourceVO> getContentSourceDetail(
            @PathVariable String sourceId,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        ContentSourceVO sourceVO = contentExtractionService.getContentSourceDetail(sourceId, userId);
        return ResponseResult.success(sourceVO);
    }

    /**
     * 获取提取详情
     *
     * @param extractionId 提取ID
     * @param request HTTP请求
     * @return 提取详情VO
     */
    @GetMapping("/extraction/{extractionId}")
    public ResponseResult<ContentExtractionDetailVO> getExtractionDetail(
            @PathVariable String extractionId,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        ContentExtractionDetailVO detailVO = contentExtractionService.getExtractionDetail(extractionId, userId);
        return ResponseResult.success(detailVO);
    }

    /**
     * 更新提取内容
     *
     * @param dto 更新DTO
     * @param request HTTP请求
     * @return 是否成功
     */
    @PutMapping("/extraction")
    public ResponseResult<Boolean> updateExtraction(
            @RequestBody ContentExtractionUpdateDTO dto,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        boolean success = contentExtractionService.updateExtraction(dto, userId);
        return ResponseResult.success(success);
    }

    /**
     * 发布为食谱
     *
     * @param extractionId 提取ID
     * @param request HTTP请求
     * @return 食谱ID
     */
    @PostMapping("/extraction/{extractionId}/publish")
    public ResponseResult<String> publishAsRecipe(
            @PathVariable String extractionId,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        String recipeId = contentExtractionService.publishAsRecipe(extractionId, userId);
        return ResponseResult.success(recipeId);
    }

    /**
     * 验证提取内容
     *
     * @param extractionId 提取ID
     * @param verified 是否验证通过
     * @param score 评分
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/extraction/{extractionId}/verify")
    public ResponseResult<Boolean> verifyExtraction(
            @PathVariable String extractionId,
            @RequestParam Boolean verified,
            @RequestParam(required = false) Integer score,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        boolean success = contentExtractionService.verifyExtraction(extractionId, verified, score, userId);
        return ResponseResult.success(success);
    }

    /**
     * 重新提取
     *
     * @param sourceId 内容源ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/source/{sourceId}/re-extract")
    public ResponseResult<Boolean> reExtract(
            @PathVariable String sourceId,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        boolean success = contentExtractionService.reExtract(sourceId, userId);
        return ResponseResult.success(success);
    }

    /**
     * 删除内容源
     *
     * @param sourceId 内容源ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @DeleteMapping("/source/{sourceId}")
    public ResponseResult<Boolean> deleteContentSource(
            @PathVariable String sourceId,
            HttpServletRequest request) {
        String userId = resolveUserId(request);

        boolean success = contentExtractionService.deleteContentSource(sourceId, userId);
        return ResponseResult.success(success);
    }

    /**
     * 查询当前用户是否已绑定小红书登录态。
     *
     * @param request HTTP请求
     * @return 绑定状态，绝不返回登录态本体
     */
    @GetMapping("/xhs-session")
    public ResponseResult<Map<String, Object>> getXhsSession(HttpServletRequest request) {
        String userId = resolveUserId(request);
        boolean bound = xhsSessionStore.exists(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bound", bound);
        data.put("updatedAt", bound ? xhsSessionStore.updatedAt(userId) : null);
        data.put("status", bound ? "active" : "none");
        return ResponseResult.success(data);
    }

    /**
     * 绑定当前用户的小红书登录态。
     *
     * @param dto 登录态
     * @param request HTTP请求
     * @return 绑定结果
     */
    @PostMapping("/xhs-session")
    public ResponseResult<Map<String, Object>> bindXhsSession(
            @RequestBody XhsSessionBindDTO dto,
            HttpServletRequest request) {
        String userId = resolveUserId(request);
        String cookie = dto == null ? null : dto.getCookie();
        validateXhsCookie(cookie);

        xhsSessionStore.write(userId, cookie);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bound", true);
        data.put("updatedAt", xhsSessionStore.updatedAt(userId));
        return ResponseResult.success(data);
    }

    /**
     * 解绑当前用户的小红书登录态。
     *
     * @param request HTTP请求
     * @return 解绑结果
     */
    @DeleteMapping("/xhs-session")
    public ResponseResult<Map<String, Object>> unbindXhsSession(HttpServletRequest request) {
        String userId = resolveUserId(request);
        xhsSessionStore.delete(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bound", false);
        return ResponseResult.success(data);
    }

    private void validateXhsCookie(String cookie) {
        if (cookie == null || cookie.isBlank()) {
            throw new BusinessException("XHS_SESSION_EMPTY", "小红书登录态不能为空");
        }
        if (cookie.length() > MAX_XHS_SESSION_LENGTH) {
            throw new BusinessException("XHS_SESSION_TOO_LONG", "小红书登录态长度不能超过 " + MAX_XHS_SESSION_LENGTH);
        }
        if (!cookie.contains("web_session")) {
            throw new BusinessException("XHS_SESSION_INVALID", "小红书登录态缺少 web_session");
        }
    }

    private String resolveUserId(HttpServletRequest request) {
        String userId = UserIdentityUtil.extractUserId(request);
        if (userId == null || userId.isBlank()) {
            return "test_user";
        }
        return userId;
    }
}

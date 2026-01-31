package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.ContentExtractionUpdateDTO;
import com.xx.jaseatschoicejava.dto.ContentSourceCreateDTO;
import com.xx.jaseatschoicejava.vo.ContentExtractionDetailVO;
import com.xx.jaseatschoicejava.vo.ContentSourceVO;

import java.util.List;

/**
 * 内容提取服务接口
 *
 * @author Claude
 * @since 2025-01-31
 */
public interface ContentExtractionService {

    /**
     * 添加内容源并开始提取
     *
     * @param dto 创建DTO
     * @param userId 用户ID
     * @return 内容源ID
     */
    String addContentSource(ContentSourceCreateDTO dto, String userId);

    /**
     * 获取用户的内容源列表
     *
     * @param userId 用户ID
     * @return 内容源列表
     */
    List<ContentSourceVO> getUserContentSources(String userId);

    /**
     * 获取内容源详情
     *
     * @param sourceId 内容源ID
     * @param userId 用户ID
     * @return 内容源VO
     */
    ContentSourceVO getContentSourceDetail(String sourceId, String userId);

    /**
     * 获取提取详情
     *
     * @param extractionId 提取ID
     * @param userId 用户ID
     * @return 提取详情VO
     */
    ContentExtractionDetailVO getExtractionDetail(String extractionId, String userId);

    /**
     * 更新提取内容
     *
     * @param dto 更新DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateExtraction(ContentExtractionUpdateDTO dto, String userId);

    /**
     * 发布为食谱
     *
     * @param extractionId 提取ID
     * @param userId 用户ID
     * @return 食谱ID
     */
    String publishAsRecipe(String extractionId, String userId);

    /**
     * 验证提取内容
     *
     * @param extractionId 提取ID
     * @param verified 是否验证通过
     * @param score 评分（1-5）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean verifyExtraction(String extractionId, Boolean verified, Integer score, String userId);

    /**
     * 重新提取
     *
     * @param sourceId 内容源ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean reExtract(String sourceId, String userId);

    /**
     * 删除内容源
     *
     * @param sourceId 内容源ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteContentSource(String sourceId, String userId);

    /**
     * 处理待提取任务（定时任务调用）
     *
     * @return 处理的任务数量
     */
    int processPendingTasks();
}

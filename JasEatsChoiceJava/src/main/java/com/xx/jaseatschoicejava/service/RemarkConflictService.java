package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.RemarkConflictCheckDTO;
import com.xx.jaseatschoicejava.vo.RemarkConflictCheckVO;

/**
 * 备注冲突检测服务接口
 *
 * @author Claude
 * @since 2025-01-30
 */
public interface RemarkConflictService {

    /**
     * 检测备注冲突
     *
     * @param dto 检测请求DTO
     * @return 检测结果VO
     */
    RemarkConflictCheckVO checkConflict(RemarkConflictCheckDTO dto);

    /**
     * 解析备注中的口味标签
     *
     * @param remark 备注内容
     * @return 口味标签列表
     */
    java.util.List<String> parseTasteTags(String remark);

    /**
     * 获取菜品的推荐口味标签
     *
     * @param dishId 菜品ID
     * @return 推荐标签列表
     */
    java.util.List<String> getRecommendedTasteTags(String dishId);

    /**
     * 格式化备注（标准化口味标签）
     *
     * @param originalRemark 原始备注
     * @param tasteTags 选中的口味标签
     * @return 格式化后的备注
     */
    String formatRemark(String originalRemark, java.util.List<String> tasteTags);
}

package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.RemarkConflictCheckDTO;
import com.xx.jaseatschoicejava.service.RemarkConflictService;
import com.xx.jaseatschoicejava.vo.RemarkConflictCheckVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 备注冲突检测控制器
 *
 * @author Claude
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/v1/remark-conflict")
@Api(tags = "备注冲突检测")
public class RemarkConflictController {

    private static final Logger log = LoggerFactory.getLogger(RemarkConflictController.class);

    @Autowired
    private RemarkConflictService remarkConflictService;

    @PostMapping("/check")
    @ApiOperation("检测备注冲突")
    public ResponseResult<RemarkConflictCheckVO> checkConflict(@RequestBody RemarkConflictCheckDTO dto) {
        try {
            RemarkConflictCheckVO result = remarkConflictService.checkConflict(dto);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("检测备注冲突失败", e);
            return ResponseResult.fail("500", "检测失败：" + e.getMessage());
        }
    }

    @PostMapping("/parse-tags")
    @ApiOperation("解析备注中的口味标签")
    public ResponseResult<List<String>> parseTasteTags(
            @ApiParam("备注内容") @RequestParam String remark) {
        try {
            List<String> tags = remarkConflictService.parseTasteTags(remark);
            return ResponseResult.success(tags);
        } catch (Exception e) {
            log.error("解析口味标签失败", e);
            return ResponseResult.fail("500", "解析失败：" + e.getMessage());
        }
    }

    @GetMapping("/recommended-tags/{dishId}")
    @ApiOperation("获取菜品推荐口味标签")
    public ResponseResult<List<String>> getRecommendedTasteTags(
            @ApiParam("菜品ID") @PathVariable String dishId) {
        try {
            List<String> tags = remarkConflictService.getRecommendedTasteTags(dishId);
            return ResponseResult.success(tags);
        } catch (Exception e) {
            log.error("获取推荐标签失败", e);
            return ResponseResult.fail("500", "获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/format")
    @ApiOperation("格式化备注（标准化口味标签）")
    public ResponseResult<String> formatRemark(
            @ApiParam("原始备注") @RequestParam String originalRemark,
            @ApiParam("选中的口味标签") @RequestParam List<String> tasteTags) {
        try {
            String formatted = remarkConflictService.formatRemark(originalRemark, tasteTags);
            return ResponseResult.success(formatted);
        } catch (Exception e) {
            log.error("格式化备注失败", e);
            return ResponseResult.fail("500", "格式化失败：" + e.getMessage());
        }
    }
}

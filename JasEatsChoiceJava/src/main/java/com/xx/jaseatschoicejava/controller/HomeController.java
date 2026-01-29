package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.HotTopicResponse;
import com.xx.jaseatschoicejava.service.HotTopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/v1/home")
@Api(tags = "首页管理")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private HotTopicService hotTopicService;

    /**
     * 获取今日热点（包含详细信息）
     * 使用组合策略：数据库配置 > 热门教程 > AI生成 > 默认热点
     *
     * @return 今日热点响应对象
     */
    @GetMapping("/hot-topic")
    @ApiOperation("获取今日热点")
    public ResponseResult<HotTopicResponse> getHotTopic() {
        try {
            HotTopicResponse hotTopic = hotTopicService.getTodayHotTopic();
            log.info("返回今日热点: {}", hotTopic.getContent());
            return ResponseResult.success(hotTopic);
        } catch (Exception e) {
            log.error("获取今日热点失败", e);
            return ResponseResult.fail("500", "获取今日热点失败");
        }
    }

    /**
     * 记录热点点击
     *
     * @param params 请求参数，包含 content 字段
     * @return 操作结果
     */
    @PostMapping("/hot-topic/click")
    @ApiOperation("记录热点点击")
    public ResponseResult<Void> recordClick(@RequestBody Map<String, String> params) {
        try {
            String content = params.get("content");
            hotTopicService.recordClick(content);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("记录热点点击失败", e);
            return ResponseResult.fail("500", "记录失败");
        }
    }

    /**
     * 记录热点分享
     *
     * @param params 请求参数，包含 content 字段
     * @return 操作结果
     */
    @PostMapping("/hot-topic/share")
    @ApiOperation("记录热点分享")
    public ResponseResult<Void> recordShare(@RequestBody Map<String, String> params) {
        try {
            String content = params.get("content");
            hotTopicService.recordShare(content);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("记录热点分享失败", e);
            return ResponseResult.fail("500", "记录失败");
        }
    }
}

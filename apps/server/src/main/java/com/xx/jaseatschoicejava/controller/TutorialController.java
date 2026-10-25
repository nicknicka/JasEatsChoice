package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教程控制器 - 用户端接口
 */
@RestController
@RequestMapping("/v1/tutorial")
public class TutorialController {

    @Autowired
    private TutorialService tutorialService;

    /**
     * 获取首页精选教程
     * GET /api/v1/tutorial/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Tutorial>> getFeaturedTutorials() {
        List<Tutorial> tutorials = tutorialService.getFeaturedTutorials();
        return ResponseEntity.ok(tutorials);
    }

    /**
     * 获取所有已发布的教程
     * GET /api/v1/tutorial/list
     */
    @GetMapping("/list")
    public ResponseEntity<List<Tutorial>> getAllTutorials() {
        List<Tutorial> tutorials = tutorialService.getAllTutorials();
        return ResponseEntity.ok(tutorials);
    }

    /**
     * 获取教程详情
     * GET /api/v1/tutorial/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tutorial> getTutorialDetail(@PathVariable String id) {
        Tutorial tutorial = tutorialService.getTutorialDetail(id);
        if (tutorial == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tutorial);
    }

    /**
     * 分页查询教程
     * GET /api/v1/tutorial/page?page=1&size=10&sourceType=ADMIN&status=PUBLISHED
     */
    @GetMapping("/page")
    public ResponseEntity<Page<Tutorial>> getTutorialsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String status) {
        Page<Tutorial> tutorials = tutorialService.getTutorialsByPage(page, size, sourceType, status);
        return ResponseEntity.ok(tutorials);
    }
}

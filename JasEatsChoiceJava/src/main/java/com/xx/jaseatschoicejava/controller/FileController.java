package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.config.FileUploadConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 处理所有上传文件的访问请求
 */
@Api(tags = "文件访问模块")
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 访问上传的文件
     * 支持两种路径格式：
     * 1. /files/{filename} - 直接访问上传根目录下的文件
     * 2. /files/{category}/{filename} - 访问分类目录下的文件
     *
     * @param path 文件路径（可包含目录）
     * @return 文件内容
     */
    @ApiOperation("访问上传的文件")
    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(@PathVariable(required = false) String path) {
        try {
            // 获取上传目录的绝对路径
            String uploadPath = fileUploadConfig.getUploadPath();
            File uploadDir = new File(uploadPath);
            String absoluteUploadPath = uploadDir.getAbsolutePath();

            // 确保路径以文件分隔符结尾
            if (!absoluteUploadPath.endsWith(File.separator)) {
                absoluteUploadPath += File.separator;
            }

            // 构建完整的文件路径
            String fullPath = absoluteUploadPath + path;

            // 安全检查：防止路径遍历攻击
            File file = new File(fullPath);
            if (!file.exists() || !file.isFile()) {
                // 如果直接访问失败，尝试在chat目录下查找
                File chatFile = new File(absoluteUploadPath + "chat" + File.separator + path);
                if (chatFile.exists() && chatFile.isFile()) {
                    file = chatFile;
                } else {
                    return ResponseEntity.notFound().build();
                }
            }

            // 检查文件是否在上传目录内（防止路径遍历）
            if (!file.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 确定文件类型
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 创建资源
            Resource resource = new FileSystemResource(file);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(file.length());

            // 对于图片，设置缓存
            if (contentType.startsWith("image/")) {
                headers.setCacheControl("public, max-age=86400"); // 缓存1天
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 访问上传的图片（兼容旧接口）
     * 路径格式：/files/image/{filename}
     *
     * @param filename 文件名
     * @return 图片内容
     */
    @ApiOperation("访问上传的图片")
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        return getFile(filename);
    }

    /**
     * 访问聊天图片
     * 路径格式：/files/chat/{filename}
     *
     * @param filename 文件名
     * @return 图片内容
     */
    @ApiOperation("访问聊天图片")
    @GetMapping("/chat/{filename}")
    public ResponseEntity<Resource> getChatImage(@PathVariable String filename) {
        return getFile("chat/" + filename);
    }
}

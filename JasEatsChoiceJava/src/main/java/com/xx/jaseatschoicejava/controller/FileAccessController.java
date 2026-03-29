package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.config.FileUploadConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
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
import java.util.Arrays;
import java.util.List;

/**
 * 文件访问控制器（兼容模式）
 * 处理旧的直接文件名访问方式，自动在多个目录中查找文件
 *
 * 支持的访问路径：
 * - /api/files/abc123.png - 标准访问方式
 * - /api/abc123.png - 兼容旧方式，自动查找
 *
 * 优先级：设置为最高优先级，确保在静态资源处理之前拦截文件访问请求
 */
@Api(tags = "文件访问兼容模块")
@RestController
@Order(1) // 设置最高优先级
public class FileAccessController {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 要搜索的子目录列表（按优先级排序）
     */
    private static final List<String> SEARCH_SUBDIRS = Arrays.asList(
            "",           // 根目录
            "chat",       // 聊天图片目录
            "avatar",     // 头像目录
            "dish",       // 菜品目录
            "dish-recognition", // 菜品识别目录
            "admin"       // 管理员目录
    );

    /**
     * 访问文件（自动查找）- 通过 /files/ 路径
     *
     * @param filename 文件名（可能包含路径，如 "chat/abc123.png"）
     * @return 文件内容
     */
    @ApiOperation("访问上传的文件（标准路径）")
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> accessFile(@PathVariable String filename) {
        try {
            // 获取上传目录的绝对路径
            String uploadPath = fileUploadConfig.getUploadPath();
            File uploadDir = new File(uploadPath);
            String absoluteUploadPath = uploadDir.getCanonicalPath();

            // 确保路径以文件分隔符结尾
            if (!absoluteUploadPath.endsWith(File.separator)) {
                absoluteUploadPath += File.separator;
            }

            // 如果文件名已经包含路径（如 "chat/abc123.png"），直接访问
            if (filename.contains(File.separator)) {
                File file = new File(absoluteUploadPath + filename);
                if (file.exists() && file.isFile()) {
                    return serveFile(file);
                }
            }

            // 否则，在各个子目录中查找文件
            for (String subdir : SEARCH_SUBDIRS) {
                String fullPath = absoluteUploadPath;
                if (!subdir.isEmpty()) {
                    fullPath += subdir + File.separator;
                }
                fullPath += filename;

                File file = new File(fullPath);
                if (file.exists() && file.isFile()) {
                    System.out.println("✅ 找到文件: " + fullPath);
                    return serveFile(file);
                }
            }

            // 文件未找到
            System.out.println("❌ 文件未找到: " + filename);
            System.out.println("   已搜索路径:");
            for (String subdir : SEARCH_SUBDIRS) {
                String searchPath = subdir.isEmpty() ? "根目录" : subdir;
                System.out.println("   - " + searchPath);
            }

            return ResponseEntity.notFound().build();

        } catch (IOException e) {
            System.err.println("❌ 文件访问错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 提供文件下载
     */
    private ResponseEntity<Resource> serveFile(File file) throws IOException {
        // 安全检查：确保文件在上传目录内
        String uploadPath = fileUploadConfig.getUploadPath();
        File uploadDir = new File(uploadPath);

        if (!file.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
            System.err.println("❌ 安全警告：尝试访问上传目录外的文件: " + file.getCanonicalPath());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 确定文件类型
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            // 根据文件扩展名确定类型
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else {
                contentType = "application/octet-stream";
            }
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
    }

    /**
     * 访问文件（兼容旧方式）- 直接通过文件名访问
     * 例如：/api/abc123.png 会自动在 uploads、uploads/chat、uploads/avatar 等目录中查找
     *
     * 匹配规则：
     * - 文件名可以包含字母、数字、连字符、下划线
     * - 必须包含文件扩展名（如 .png, .jpg, .pdf 等）
     * - 支持 UUID 格式的文件名（如 1768052638512-90f14fcc-f1ac-417d-be63-12f77fc7872b.png）
     *
     * @param filename 文件名（不含路径）
     * @return 文件内容
     */
    @ApiOperation("访问上传的文件（兼容旧方式）")
    @GetMapping("/{filename:[\\w-]+\\.[\\w]{2,5}}") // 匹配：字母数字下划线连字符.扩展名
    public ResponseEntity<Resource> accessFileCompat(@PathVariable String filename) {
        System.out.println("📥 [FileAccess] 兼容模式访问: " + filename);
        return accessFile(filename);
    }
}
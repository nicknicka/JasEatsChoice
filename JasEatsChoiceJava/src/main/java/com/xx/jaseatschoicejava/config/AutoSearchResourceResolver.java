package com.xx.jaseatschoicejava.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 自动搜索资源的解析器
 * 当请求的文件在指定位置找不到时，自动在多个子目录中查找
 *
 * 使用场景：
 * - 前端请求 /api/abc123.png
 * - 系统自动在 uploads/、uploads/chat/、uploads/avatar/ 等目录中查找
 */
public class AutoSearchResourceResolver implements ResourceResolver {

    private final ResourceLoader resourceLoader;
    private final String uploadPath;

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

    public AutoSearchResourceResolver(ResourceLoader resourceLoader, String uploadPath) {
        this.resourceLoader = resourceLoader;
        this.uploadPath = uploadPath;
    }

    @Override
    public Resource resolveResource(
            HttpServletRequest request,
            String requestPath,
            List<? extends Resource> locations,
            ResourceResolverChain chain) {

        // 如果请求路径包含子目录（如 "chat/abc123.png"），直接使用
        if (requestPath.contains("/")) {
            Resource directResource = resourceLoader.getResource("file:" + uploadPath + requestPath);
            if (directResource.exists() && directResource.isReadable()) {
                return directResource;
            }
        }

        // 否则，在各个子目录中搜索
        for (String subdir : SEARCH_SUBDIRS) {
            String searchPath = subdir.isEmpty() ? requestPath : subdir + "/" + requestPath;
            Resource foundResource = resourceLoader.getResource("file:" + uploadPath + searchPath);

            if (foundResource.exists() && foundResource.isReadable()) {
                System.out.println("✅ [AutoSearch] 找到文件: " + searchPath);
                return foundResource;
            }
        }

        System.out.println("❌ [AutoSearch] 未找到文件: " + requestPath);
        return null;
    }

    @Override
    public String resolveUrlPath(
            String resourcePath,
            List<? extends Resource> locations,
            ResourceResolverChain chain) {

        // 委托给链中的下一个解析器
        return chain.resolveUrlPath(resourcePath, locations);
    }
}

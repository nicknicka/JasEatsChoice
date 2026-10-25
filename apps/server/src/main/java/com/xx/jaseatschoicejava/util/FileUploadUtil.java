package com.xx.jaseatschoicejava.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.xx.jaseatschoicejava.config.FileUploadConfig;

import cn.hutool.http.HttpUtil;

@Component
public class FileUploadUtil {

    private static FileUploadConfig fileUploadConfig;

    @Autowired
    public void setFileUploadConfig(FileUploadConfig config) {
        FileUploadUtil.fileUploadConfig = config;
    }

    /**
     * 上传文件并返回相对URL路径
     *
     * @param file     上传的文件
     * @param category 文件分类目录（如：chat, avatar等）
     * @param userId 用户ID（用于分类存储）
     * @return 相对URL路径（相对于uploads目录）
     */
    public static String uploadFile(MultipartFile file, String category, String userId) throws IOException {
        // 获取配置的上传基础路径
        String baseUploadPath = fileUploadConfig.getUploadPath();

        // 构建完整的上传目录路径：baseUploadPath/category/userId/
        String finalUploadDir = baseUploadPath + category + "/";
        if (userId != null && !userId.isEmpty()) {
            finalUploadDir += userId + "/";
        }

        // 检查目录是否存在
        File directory = new File(finalUploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成新的文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        // 保存文件
        File dest = new File(finalUploadDir + fileName);
        file.transferTo(dest);

        // 返回相对URL路径：category/userId/fileName 或 category/fileName
        String relativePath = category + "/";
        if (userId != null && !userId.isEmpty()) {
            relativePath += userId + "/";
        }
        return relativePath + fileName;
    }

    /**
     * 上传文件并返回相对URL路径（兼容旧接口）
     *
     * @param file     上传的文件
     * @param category 文件分类目录
     * @return 相对URL路径
     */
    public static String uploadFile(MultipartFile file, String category) throws IOException {
        return uploadFile(file, category, null);
    }

    /**
     * 上传图片并返回相对URL路径
     *
     * @param file     上传的图片
     * @param category 图片分类目录
     * @param userId 用户ID（用于分类存储）
     * @return 相对URL路径
     * @throws IOException 文件上传异常
     */
    public static String uploadImage(MultipartFile file, String category, String userId) throws IOException {
        // 验证图片类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("文件不是有效的图片类型");
        }

        return uploadFile(file, category, userId);
    }

    /**
     * 上传图片并返回相对URL路径（兼容旧接口）
     *
     * @param file     上传的图片
     * @param category 图片分类目录
     * @return 相对URL路径
     * @throws IOException 文件上传异常
     */
    public static String uploadImage(MultipartFile file, String category) throws IOException {
        return uploadImage(file, category, null);
    }

    /**
     * 上传base64格式图片并返回文件名
     *
     * @param base64Str base64图片字符串
     * @param uploadDir 上传目录
     * @return 保存后的文件名
     * @throws IOException 文件上传异常
     */
    /**
     * 上传base64格式图片并返回文件名
     *
     * @param base64Str base64图片字符串
     * @param uploadDir 上传目录
     * @param userId 用户ID（用于分类存储）
     * @return 保存后的文件名
     * @throws IOException 文件上传异常
     */
    public static String uploadBase64Image(String base64Str, String uploadDir, String userId) throws IOException {
        // 验证base64字符串格式
        if (base64Str == null || base64Str.isEmpty()) {
            throw new IllegalArgumentException("base64字符串不能为空");
        }

        // 去除base64字符串前缀，如 "data:image/png;base64,"
        String processedBase64Str = base64Str;
        if (base64Str.contains(",")) {
            processedBase64Str = base64Str.substring(base64Str.indexOf(",") + 1);
        }

        // 构建最终的上传目录，如果提供了userId则按用户ID分类
        String finalUploadDir = uploadDir;
        if (userId != null && !userId.isEmpty()) {
            finalUploadDir = uploadDir + userId + "/";
        }

        // 检查目录是否存在
        File directory = new File(finalUploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成新的文件名
        String suffix = getImageSuffixFromBase64(base64Str);
        String fileName = UUID.randomUUID().toString() + suffix;

        // 解码并保存图片
        byte[] imageBytes = java.util.Base64.getDecoder().decode(processedBase64Str);
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(finalUploadDir + fileName)) {
            fos.write(imageBytes);
        }

        // 返回带用户目录的文件名
        return (userId != null && !userId.isEmpty() ? userId + "/" : "") + fileName;
    }

    /**
     * 下载远程图片并保存到本地 uploads 目录，返回可访问的完整 URL。
     *
     * @param imageUrl 远程图片地址
     * @param category 保存目录分类
     * @param userId 用户ID（用于分类存储）
     * @return 可访问的完整图片 URL
     * @throws IOException 文件保存异常
     */
    public static String uploadRemoteImage(String imageUrl, String category, String userId) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }

        byte[] imageBytes = HttpUtil.downloadBytes(imageUrl);
        return uploadImageBytes(imageBytes, imageUrl, category, userId);
    }

    /**
     * 将图片字节保存到本地 uploads 目录，返回可访问的完整 URL。
     *
     * @param imageBytes 图片字节
     * @param sourceName 来源名称或URL，用于推断后缀
     * @param category 保存目录分类
     * @param userId 用户ID（用于分类存储）
     * @return 可访问的完整图片 URL
     * @throws IOException 文件保存异常
     */
    public static String uploadImageBytes(byte[] imageBytes, String sourceName, String category, String userId) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片内容不能为空");
        }

        String baseUploadPath = fileUploadConfig.getUploadPath();

        String finalUploadDir = baseUploadPath + category + "/";
        if (userId != null && !userId.isEmpty()) {
            finalUploadDir += userId + "/";
        }

        File directory = new File(finalUploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String suffix = resolveImageSuffix(sourceName, imageBytes);
        String fileName = UUID.randomUUID().toString() + suffix;

        File dest = new File(finalUploadDir + fileName);
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
            fos.write(imageBytes);
        }

        String relativePath = category + "/";
        if (userId != null && !userId.isEmpty()) {
            relativePath += userId + "/";
        }
        relativePath += fileName;

        return buildPublicUrl(relativePath);
    }

    /**
     * 上传base64格式图片并返回文件名（兼容旧接口）
     *
     * @param base64Str base64图片字符串
     * @param uploadDir 上传目录
     * @return 保存后的文件名
     * @throws IOException 文件上传异常
     */
    public static String uploadBase64Image(String base64Str, String uploadDir) throws IOException {
        return uploadBase64Image(base64Str, uploadDir, null);
    }

    /**
     * 从base64字符串获取图片后缀
     *
     * @param base64Str base64图片字符串
     * @return 图片后缀
     */
    private static String getImageSuffixFromBase64(String base64Str) {
        if (base64Str.startsWith("data:image/png;")) {
            return ".png";
        } else if (base64Str.startsWith("data:image/jpeg;") || base64Str.startsWith("data:image/jpg;")) {
            return ".jpg";
        } else if (base64Str.startsWith("data:image/gif;")) {
            return ".gif";
        } else {
            // 默认使用.png
            return ".png";
        }
    }

    private static String resolveImageSuffix(String sourceName, byte[] imageBytes) {
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
            if (mimeType != null) {
                if (mimeType.contains("png")) {
                    return ".png";
                }
                if (mimeType.contains("gif")) {
                    return ".gif";
                }
                if (mimeType.contains("webp")) {
                    return ".webp";
                }
                if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
                    return ".jpg";
                }
            }
        } catch (IOException ignored) {
            // 继续使用文件名兜底
        }

        if (sourceName != null) {
            String lower = sourceName.toLowerCase();
            if (lower.contains(".png")) return ".png";
            if (lower.contains(".gif")) return ".gif";
            if (lower.contains(".webp")) return ".webp";
            if (lower.contains(".jpg") || lower.contains(".jpeg")) return ".jpg";
        }

        return ".jpg";
    }

    private static String buildPublicUrl(String relativePath) {
        String prefix = fileUploadConfig.getUrlPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "/api/uploads/";
        }

        String serverUrl = fileUploadConfig.getServerUrl();
        if (serverUrl == null || serverUrl.isEmpty()) {
            return prefix + relativePath;
        }

        String normalizedServerUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        String normalizedPrefix = prefix.startsWith("/") ? prefix : "/" + prefix;
        return normalizedServerUrl + normalizedPrefix + relativePath;
    }
}

package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUploadConfig {

    /**
     * 文件上传路径
     */
    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 访问URL前缀（相对路径）
     */
    @Value("${file.upload.url-prefix:/api/uploads/}")
    private String urlPrefix;

    /**
     * 服务器完整URL
     */
    @Value("${file.upload.server-url:http://localhost:8080}")
    private String serverUrl;

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}

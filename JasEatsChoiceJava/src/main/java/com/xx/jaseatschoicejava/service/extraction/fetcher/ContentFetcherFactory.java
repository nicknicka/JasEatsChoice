package com.xx.jaseatschoicejava.service.extraction.fetcher;

import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 内容抓取策略工厂
 * 根据 platform + contentType 选择对应的 Fetcher 实现
 */
@Slf4j
@Component
public class ContentFetcherFactory {

    private final List<ContentFetcher> fetchers;

    public ContentFetcherFactory(List<ContentFetcher> fetchers) {
        this.fetchers = fetchers;
    }

    /**
     * 获取适合的抓取器
     *
     * @param platform    来源平台
     * @param contentType 内容类型
     * @return 对应的 Fetcher 实现
     */
    public ContentFetcher getFetcher(ContentPlatform platform, ContentType contentType) {
        return fetchers.stream()
            .filter(f -> f.canHandle(platform, contentType))
            .findFirst()
            .orElseGet(() -> {
                log.warn("未找到匹配的抓取器: platform={}, contentType={}，使用兜底策略", platform, contentType);
                // 兜底：返回第一个通用抓取器
                return fetchers.stream()
                    .filter(f -> f instanceof ArticleContentFetcher)
                    .findFirst()
                    .orElse(fetchers.get(0));
            });
    }
}

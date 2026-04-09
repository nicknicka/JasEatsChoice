package com.xx.jaseatschoicejava.service.extraction.fetcher;

import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

/**
 * 内容抓取策略接口
 * 不同平台/内容类型实现不同的抓取逻辑
 */
public interface ContentFetcher {

    /**
     * 判断是否能处理该类型的内容
     *
     * @param platform    来源平台
     * @param contentType 内容类型
     * @return 是否能处理
     */
    boolean canHandle(ContentPlatform platform, ContentType contentType);

    /**
     * 抓取内容
     *
     * @param url 内容URL
     * @return 抓取结果
     */
    FetchedContent fetch(String url);
}

package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容平台枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum ContentPlatform {

    /**
     * 抖音
     */
    DOUYIN("DOUYIN", "抖音", "https://www.douyin.com"),

    /**
     * 小红书
     */
    XIAOHONGSHU("XIAOHONGSHU", "小红书", "https://www.xiaohongshu.com"),

    /**
     * 哔哩哔哩
     */
    BILIBILI("BILIBILI", "哔哩哔哩", "https://www.bilibili.com"),

    /**
     * 微信公众号
     */
    WECHAT("WECHAT", "微信", "https://mp.weixin.qq.com"),

    /**
     * 今日头条
     */
    TOUTIAO("TOUTIAO", "今日头条", "https://www.toutiao.com"),

    /**
     * 快手
     */
    KUAISHOU("KUAISHOU", "快手", "https://www.kuaishou.com"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他", "");

    /**
     * 平台代码
     */
    private final String code;

    /**
     * 平台名称
     */
    private final String name;

    /**
     * 平台首页URL
     */
    private final String baseUrl;

    /**
     * 根据代码获取枚举
     */
    public static ContentPlatform getByCode(String code) {
        for (ContentPlatform platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }
        return OTHER;
    }

    /**
     * 根据URL判断平台
     */
    public static ContentPlatform parseFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return OTHER;
        }

        String lowerUrl = url.toLowerCase();

        if (lowerUrl.contains("douyin.com")) {
            return DOUYIN;
        } else if (lowerUrl.contains("xiaohongshu.com")) {
            return XIAOHONGSHU;
        } else if (lowerUrl.contains("bilibili.com")) {
            return BILIBILI;
        } else if (lowerUrl.contains("mp.weixin.qq.com") || lowerUrl.contains("weixin.qq.com")) {
            return WECHAT;
        } else if (lowerUrl.contains("toutiao.com")) {
            return TOUTIAO;
        } else if (lowerUrl.contains("kuaishou.com")) {
            return KUAISHOU;
        }

        return OTHER;
    }
}

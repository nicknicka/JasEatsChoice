package com.xx.jaseatschoicejava.service.bridge;

import java.util.List;

/**
 * GitHub Issue 适配器端口。测试用假实现替代，不接触真实 GitHub。
 */
public interface GitHubIssueClient {

    /**
     * 创建 Issue，返回编号与地址。
     */
    CreatedIssue createIssue(String repo, String title, String body, List<String> labels);

    /**
     * 读取 Issue 当前状态（open/closed）。
     */
    String getState(String repo, String issueNumber);

    /**
     * 创建结果。
     */
    record CreatedIssue(String number, String url) {
    }
}

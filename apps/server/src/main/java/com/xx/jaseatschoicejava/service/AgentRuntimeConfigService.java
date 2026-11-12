package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.dto.AgentRuntimeHealthVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Agent 运行时状态（只读）。
 *
 * <p>agent 的模型 / 密钥 / 策略配置真相在 DSH（另一个运行时），JAS 不再保存任何 agent 配置；
 * 本服务只探测主机侧健康并回显固定硬边界。
 */
@Service
public class AgentRuntimeConfigService {

    @Autowired
    private GitHubBridgeProperties gitHubBridgeProperties;

    @Value("${agent.runtime.dsh-profile-path:}")
    private String dshProfilePath;

    @Value("${agent.runtime.isolated-home:}")
    private String isolatedHome;

    /**
     * 状态页数据：主机侧健康 + 硬边界（不含任何可写配置）。
     */
    public AgentRuntimeHealthVO getHealth() {
        AgentRuntimeHealthVO vo = new AgentRuntimeHealthVO();

        boolean githubOk = gitHubBridgeProperties.isConfigured();
        vo.setGithub(new AgentRuntimeHealthVO.Health("GitHub", githubOk,
                githubOk ? "token 与仓库已配置" : "未配置 token 与仓库"));

        boolean profileOk = pathExists(dshProfilePath);
        vo.setDshProfile(new AgentRuntimeHealthVO.Health("DSH profile", profileOk,
                profileOk ? "profile 目录存在" : "未找到 DSH profile"));

        boolean isolatedOk = pathExists(isolatedHome);
        vo.setIsolatedAccount(new AgentRuntimeHealthVO.Health("隔离账号", isolatedOk,
                isolatedOk ? "专用账号 home 存在" : "未找到专用账号 home"));

        // 硬边界固定为开启（enabled=true 表示限制生效），页面不能关闭。
        // 真实生效依赖 DSH 部署与 agent 账号权限，本页只做展示。
        vo.setBoundaries(List.of(
                new AgentRuntimeHealthVO.Boundary("no-merge", "禁止代理合并 PR", true),
                new AgentRuntimeHealthVO.Boundary("no-deploy", "禁止代理部署", true),
                new AgentRuntimeHealthVO.Boundary("no-production-data", "禁止访问生产数据", true)));
        return vo;
    }

    private static boolean pathExists(String path) {
        return path != null && !path.isBlank() && Files.isDirectory(Path.of(path));
    }
}

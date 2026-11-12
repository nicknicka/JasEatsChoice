package com.xx.jaseatschoicejava.service.agent;

import java.util.List;
import java.util.Map;

/**
 * dsh 本地 Management API 的只读端口：只允许 GET state / GET sources。
 *
 * <p>形状对齐 dsh-superagent-prototype：路由前缀 /orchestrator/api，必须带
 * x-orchestrator-request 头；prototype 没有稳定的 tickets 列表，人工介入与系统问题
 * 从 state.health 与 sources 派生。
 */
public interface DshManagementClient {

    /** GET state：setup / health / load / paused。 */
    Map<String, Object> fetchState();

    /** GET sources：各 Source 的状态（state / health / problem）。 */
    List<Map<String, Object>> fetchSources();
}

package com.xx.jaseatschoicejava.service.agent;

import java.util.List;
import java.util.Map;

/**
 * dsh 本地 Management API 的只读端口：只允许 GET state / GET tickets。
 */
public interface DshManagementClient {

    /** GET state。 */
    Map<String, Object> fetchState();

    /** GET tickets。 */
    List<Map<String, Object>> fetchTickets();
}

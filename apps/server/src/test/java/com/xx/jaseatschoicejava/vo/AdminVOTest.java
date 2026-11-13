package com.xx.jaseatschoicejava.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Admin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 管理员列表不允许把认证凭据带出去。
 */
class AdminVOTest {

    @Test
    void serializedAdminListPayloadHasNoCredentialFields() throws Exception {
        Admin admin = new Admin();
        admin.setAdminId(1L);
        admin.setUsername("root");
        admin.setPassword("$2a$10$super-secret-hash");
        admin.setStatus("ACTIVE");

        String json = new ObjectMapper().writeValueAsString(AdminVO.from(admin));

        assertFalse(json.contains("password"), "响应里不应出现 password 字段");
        assertFalse(json.toLowerCase().contains("salt"), "响应里不应出现 salt 字段");
        assertTrue(json.contains("\"adminId\":1"));
        assertTrue(json.contains("\"username\":\"root\""));
    }
}

package com.xx.jaseatschoicejava.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;

/**
 * SecurityConfig 默认拒绝：匿名只能访问显式列出的公开接口，其余一律 401。
 * 这是 #576 的防线——新增接口忘记加注解时，失败的形态应是 401 而不是匿名 200。
 * 需要 MySQL/Redis（由 scripts/ci-backend.sh 提供）。
 */
@SpringBootTest(
    classes = JasEatsChoiceJavaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityDefaultDenyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicLoginAndCaptchaEndpointsAreReachable() throws Exception {
        mockMvc.perform(get("/v1/captcha/checkCode")).andExpect(status().isOk());
        // 登录允许匿名：验证点是不被鉴权层拦成 401（凭据校验由控制器自己做）
        mockMvc.perform(post("/v1/users/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                401, result.getResponse().getStatus(), "登录接口不应被鉴权层拦截"));
    }

    @Test
    void healthAndApiDocsAreReachable() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    /**
     * 图片直出放行：<img> 无法携带 Authorization，公开的是「读」，写入接口仍受保护。
     * 这里只断言不被鉴权层拦成 401——文件不存在时 404 也算通过。
     */
    @Test
    void imageStaticResourcesAreReadableAnonymously() throws Exception {
        for (String path : new String[] {
            "/uploads/not-exist.png",
            "/files/not-exist.png",
            "/synthetic-assets/not-exist.png"
        }) {
            int status = mockMvc.perform(get(path))
                .andReturn().getResponse().getStatus();
            org.junit.jupiter.api.Assertions.assertNotEquals(
                401, status, path + " 属于图片直出，不应被鉴权层拦成 401");
        }
        // 上传接口本身仍需登录，放行只针对静态资源读
        mockMvc.perform(post("/v1/users/1/avatar")).andExpect(status().isUnauthorized());
    }

    /**
     * 根级 /*.png 兼容入口不在最小公开集内，仍应要求登录。
     * 放开它等价于按文件名匿名遍历上传目录。
     */
    @Test
    void rootLevelFilenameAccessIsNotPublic() throws Exception {
        mockMvc.perform(get("/some-uuid.png")).andExpect(status().isUnauthorized());
    }

    @Test
    void userDataEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/v1/wallet/info/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/orders/user/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/consume-history").param("userId", "1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void browseAndAdminEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/v1/recommendations/1").param("scene", "home"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/users/search").param("keyword", "1"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/merchant")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/users")).andExpect(status().isUnauthorized());
    }
}

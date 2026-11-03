package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.xx.jaseatschoicejava.common.GlobalExceptionHandler;
import com.xx.jaseatschoicejava.config.XhsSessionStore;
import com.xx.jaseatschoicejava.service.ContentExtractionService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;

/**
 * xhs-session 接口：只回传绑定状态，绝不回传登录态本体。
 */
class ContentExtractionControllerXhsSessionTest {

    private static final String USER_ATTRIBUTE = UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE;
    private static final String SECRET = "a1=secret; web_session=session-token-xyz";

    @TempDir
    Path tempDir;

    private MockMvc mockMvc;

    private XhsSessionStore xhsSessionStore;

    @BeforeEach
    void setUp() {
        ContentExtractionController controller = new ContentExtractionController();
        ReflectionTestUtils.setField(controller, "contentExtractionService", mock(ContentExtractionService.class));
        xhsSessionStore = new XhsSessionStore(tempDir.toString());
        ReflectionTestUtils.setField(controller, "xhsSessionStore", xhsSessionStore);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void getWithoutBindingReturnsNoneStatus() throws Exception {
        mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(false))
            .andExpect(jsonPath("$.data.status").value("none"))
            .andExpect(jsonPath("$.data.updatedAt").isEmpty());
    }

    @Test
    void bindThenGetReturnsActiveAndNeverLeaksBody() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        MvcResult result = mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.status").value("active"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains(SECRET), "GET 不能回传登录态本体");
        assertFalse(body.contains("web_session"), "GET 不能回传登录态本体");
    }

    @Test
    void statusIsExpiredWhenSessionMarkedExpired() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk());

        xhsSessionStore.markExpired("user-a");

        MvcResult result = mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bound").value(true))
            .andExpect(jsonPath("$.data.status").value("expired"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains(SECRET), "GET 不能回传登录态本体");
        assertFalse(body.contains("web_session"), "GET 不能回传登录态本体");
    }

    @Test
    void rebindingClearsExpiredStatus() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk());
        xhsSessionStore.markExpired("user-a");

        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(jsonPath("$.data.status").value("active"));
    }

    @Test
    void unbindClearsExpiredStatus() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk());
        xhsSessionStore.markExpired("user-a");

        mockMvc.perform(delete("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(jsonPath("$.data.bound").value(false))
            .andExpect(jsonPath("$.data.status").value("none"));
    }

    @Test
    void unbindReturnsBoundFalse() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.bound").value(false));

        mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-a"))
            .andExpect(jsonPath("$.data.bound").value(false))
            .andExpect(jsonPath("$.data.status").value("none"));
    }

    @Test
    void rejectsBlankCookie() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"   \"}"))
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsCookieWithoutWebSession() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"a1=abc; other=def\"}"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("小红书登录态缺少 web_session"));
    }

    @Test
    void rejectsCookieLongerThanLimit() throws Exception {
        String tooLong = "web_session=" + "a".repeat(8192);

        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + tooLong + "\"}"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("小红书登录态长度不能超过 8192"));
    }

    @Test
    void sessionsAreIsolatedPerVerifiedUser() throws Exception {
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .requestAttr(USER_ATTRIBUTE, "user-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/content-extraction/xhs-session").requestAttr(USER_ATTRIBUTE, "user-b"))
            .andExpect(jsonPath("$.data.bound").value(false));
    }

    @Test
    void missingVerifiedIdentityIsRejected() throws Exception {
        // 取不到可信身份（无已验证 JWT 写入的用户ID）即视为未登录，返回 401；不再回落到共享账号。
        mockMvc.perform(post("/v1/content-extraction/xhs-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cookie\":\"" + SECRET + "\"}"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/content-extraction/xhs-session"))
            .andExpect(status().isUnauthorized());

        // 未带可信身份时伪造的 X-User-Id 请求头不得改变服务端身份
        mockMvc.perform(get("/v1/content-extraction/xhs-session").header("X-User-Id", "user-b"))
            .andExpect(status().isUnauthorized());
    }
}

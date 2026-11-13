package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 聊天上传接口的扩展名准入：白名单外的文件必须返回 400，而不是落盘后由网关直出。
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerUploadTest {

    @InjectMocks
    private ChatController controller;

    @Test
    void uploadFile_withHtmlExtension_shouldReturn400() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "exploit.html", "text/html", "<script>alert(1)</script>".getBytes());

        ResponseResult<?> result = controller.uploadFile(file);

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
    }

    @Test
    void uploadFile_withHtmlMultiSuffix_shouldReturn400() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "exploit.html.txt", "text/plain", "<script>alert(1)</script>".getBytes());

        ResponseResult<?> result = controller.uploadFile(file);

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
    }

    @Test
    void uploadImage_withHtmlExtension_shouldReturn400() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "exploit.html", "image/png", "<script>alert(1)</script>".getBytes());

        ResponseResult<?> result = controller.uploadImage(file);

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
    }
}

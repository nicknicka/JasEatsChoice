package com.xx.jaseatschoicejava.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.xx.jaseatschoicejava.exception.AiQuotaExceededException;

class AiQuotaExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsQuotaExceededToHttp429AndReadableMessage() {
        ResponseEntity<ResponseResult<?>> response =
            handler.handleAiQuotaExceeded(new AiQuotaExceededException());

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        ResponseResult<?> body = response.getBody();
        assertEquals("429", body.getCode());
        assertEquals("请求过于频繁，请稍后再试", body.getMessage());
        assertFalse(body.getSuccess());
    }
}

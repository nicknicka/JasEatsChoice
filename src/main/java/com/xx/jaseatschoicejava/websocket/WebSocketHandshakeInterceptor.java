package com.xx.jaseatschoicejava.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.xx.jaseatschoicejava.util.JwtUtil;

import java.util.Map;

/**
 * WebSocket握手拦截器，用于验证用户身份
 */
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从请求参数中获取token
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");

            // 验证token有效性
            if (token != null) {
                try {
                    // 这里只验证token是否过期，因为无法获取phone参数
                    if (!JwtUtil.isTokenExpired(token)) {
                        // 解析token获取用户ID
                        Long userId = JwtUtil.extractUserId(token);
                        // 将用户ID存储到WebSocket会话中
                        attributes.put("userId", userId.toString());
                        return true;
                    }
                } catch (Exception e) {
                    // token解析失败
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的处理
    }
}


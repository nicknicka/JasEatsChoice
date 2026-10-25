package com.xx.jaseatschoicejava.netty;

import com.xx.jaseatschoicejava.util.JwtUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket握手认证处理器
 * 在WebSocket握手升级前验证token
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public class WebSocketAuthHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthHandler.class);

    // 定义AttributeKey用于存储路径和用户ID
    public static final AttributeKey<String> PATH_KEY = AttributeKey.valueOf("wsPath");
    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    private final JwtUtil jwtUtil;

    /**
     * 构造函数，注入JwtUtil实例
     */
    public WebSocketAuthHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            // 获取URI中的token参数
            String uri = request.uri();
            logger.debug("WebSocket握手请求URI: {}", uri);

            // 提取并保存原始路径（移除查询参数）
            String originalPath = uri;
            if (uri != null && uri.contains("?")) {
                originalPath = uri.substring(0, uri.indexOf("?"));
            }

            // 验证路径是否合法（支持/ws和/ws/chat，最终都会路由到/ws）
            if (!"/ws".equals(originalPath) && !"/ws/chat".equals(originalPath)) {
                logger.warn("WebSocket握手失败：不支持的路径 - {}", originalPath);
                sendErrorResponse(ctx, request, HttpResponseStatus.NOT_FOUND, "不支持的WebSocket路径");
                return;
            }

            // 保存原始路径到Channel属性中，供后续路由使用
            ctx.channel().attr(PATH_KEY).set(originalPath);

            // 解析查询参数
            String userId = null;
            String token = null;

            if (uri != null && uri.contains("?")) {
                String queryString = uri.substring(uri.indexOf("?") + 1);
                String[] params = queryString.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];
                        if ("userId".equals(key)) {
                            userId = value;
                        } else if ("token".equals(key)) {
                            token = value;
                        }
                    }
                }
            }

            // 验证token
            if (token == null || token.isEmpty()) {
                logger.warn("WebSocket握手失败：缺少token参数");
                sendErrorResponse(ctx, request, HttpResponseStatus.UNAUTHORIZED, "缺少token参数");
                return;
            }

            try {
                // 验证token是否有效
                String tokenUserId = jwtUtil.extractUserId(token);
                if (tokenUserId == null) {
                    logger.warn("WebSocket握手失败：无法从token中提取userId");
                    sendErrorResponse(ctx, request, HttpResponseStatus.UNAUTHORIZED, "无效的token");
                    return;
                }

                // 检查userId是否匹配（可选）
                if (userId != null && !userId.isEmpty() && !userId.equals(tokenUserId)) {
                    logger.warn("WebSocket握手失败：userId不匹配 (请求: {}, token: {})", userId, tokenUserId);
                    sendErrorResponse(ctx, request, HttpResponseStatus.FORBIDDEN, "userId不匹配");
                    return;
                }

                logger.info("WebSocket认证成功: userId={}, path={}", tokenUserId, originalPath);

                // 将userId存储到Channel属性中，供后续Handler使用
                ctx.channel().attr(USER_ID_KEY).set(tokenUserId);

                // 所有WebSocket请求都统一使用/ws路径（WebSocketServerProtocolHandler只支持单一路径）
                // 但原始路径已保存在PATH_KEY中，后续业务handler可以根据原始路径路由
                request.setUri("/ws");

                // 认证成功，传递给下一个Handler
                ctx.fireChannelRead(msg);

            } catch (Exception e) {
                logger.error("WebSocket握手失败：token验证异常", e);
                sendErrorResponse(ctx, request, HttpResponseStatus.UNAUTHORIZED, "token验证失败");
                return;
            }
        } else {
            // 非HTTP请求，直接传递
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocketAuthHandler异常", cause);
        ctx.close();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, FullHttpRequest request,
                                   HttpResponseStatus status, String message) {
        io.netty.handler.codec.http.FullHttpResponse response = new io.netty.handler.codec.http.DefaultFullHttpResponse(
                request.protocolVersion(),
                status,
                ctx.alloc().buffer()
        );
        response.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
        ctx.close();
    }
}

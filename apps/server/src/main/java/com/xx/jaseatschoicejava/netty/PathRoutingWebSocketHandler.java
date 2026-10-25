package com.xx.jaseatschoicejava.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket路径路由处理器
 * 根据握手时的路径将消息路由到不同的业务处理器
 *
 * @Author nickxiao
 * @Date 2025/01/19
 */
public class PathRoutingWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(PathRoutingWebSocketHandler.class);

    private final NettyChatHandler chatHandler;
    private final NettyOrderHandler orderHandler;

    public PathRoutingWebSocketHandler(NettyChatHandler chatHandler, NettyOrderHandler orderHandler) {
        this.chatHandler = chatHandler;
        this.orderHandler = orderHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取握手时的原始路径
        String path = (String) ctx.channel().attr(WebSocketAuthHandler.PATH_KEY).get();

        if (path == null) {
            logger.warn("无法获取WebSocket路径，使用默认处理器");
            return;
        }

        // 根据路径路由到不同的处理器
        if ("/ws/chat".equals(path)) {
            logger.debug("路由到聊天处理器: {}", path);
            chatHandler.channelRead0(ctx, msg);
        } else if ("/ws".equals(path)) {
            logger.debug("路由到订单处理器: {}", path);
            orderHandler.channelRead0(ctx, msg);
        } else {
            logger.warn("未知的WebSocket路径: {}", path);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理WebSocket握手事件
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            String path = ctx.channel().attr(WebSocketAuthHandler.PATH_KEY).get();
            logger.info("WebSocket握手完成，路径: {}", path);

            // 通知对应的业务处理器
            if ("/ws/chat".equals(path)) {
                chatHandler.userEventTriggered(ctx, evt);
            } else if ("/ws".equals(path)) {
                orderHandler.userEventTriggered(ctx, evt);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String path = ctx.channel().attr(WebSocketAuthHandler.PATH_KEY).get();
        logger.info("客户端断开连接，路径: {}", path);

        // 通知对应的业务处理器
        if ("/ws/chat".equals(path)) {
            chatHandler.channelInactive(ctx);
        } else if ("/ws".equals(path)) {
            orderHandler.channelInactive(ctx);
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocket异常", cause);
        ctx.close();
    }
}

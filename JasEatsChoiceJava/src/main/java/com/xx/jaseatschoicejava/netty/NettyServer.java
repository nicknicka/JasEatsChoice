package com.xx.jaseatschoicejava.netty;

import com.xx.jaseatschoicejava.config.NettyConfig;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务器
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
@Component
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private NettyConfig nettyConfig;

    @Autowired
    private ChatMsgService chatMsgService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NettyChatHandler chatHandler;

    @Autowired
    private NettyOrderHandler orderHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup businessGroup;
    private Channel serverChannel;

    // 防止重复启动的标志
    private volatile boolean started = false;

    // 防止重复关闭的标志
    private volatile boolean shutdown = false;

    /**
     * 启动Netty服务器
     */
    public void start() throws InterruptedException {
        // 避免重复启动
        if (started) {
            logger.info("Netty server is already running, skip starting");
            return;
        }

        // 创建BossGroup和WorkerGroup
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossGroupThreads());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerGroupThreads());

        // 创建业务线程池
        businessGroup = new DefaultEventExecutorGroup(16);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 允许立即重用端口，解决TIME_WAIT问题
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // HTTP请求解码器和编码器
                            pipeline.addLast(new io.netty.handler.codec.http.HttpServerCodec());
                            // HTTP请求聚合器
                            pipeline.addLast(new io.netty.handler.codec.http.HttpObjectAggregator(65536));
                            // WebSocket握手认证处理器（在WebSocket协议处理器之前）
                            pipeline.addLast(new WebSocketAuthHandler(jwtUtil));
                            // WebSocket协议处理器，统一使用/ws路径
                            pipeline.addLast(new io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler("/ws"));
                            // 字符串解码器
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            // 字符串编码器
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));

                            // 路径路由处理器（根据原始路径分发到不同的业务Handler）
                            pipeline.addLast(businessGroup, new PathRoutingWebSocketHandler(chatHandler, orderHandler));
                        }
                    });

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(nettyConfig.getPort()).sync();
            serverChannel = future.channel();

            // 设置服务器已启动标志
            started = true;

            logger.info("Netty server started on port: {}", nettyConfig.getPort());

            // 等待服务器关闭
            // future.channel().closeFuture().sync();
        } finally {
            // 服务器关闭时的处理已经移到@PreDestroy方法中
        }
    }

    /**
     * 关闭Netty服务器
     */
    @PreDestroy
    public void shutdown() {
        // 避免重复关闭
        if (shutdown) {
            logger.info("Netty server is already shutting down or has been shut down");
            return;
        }

        logger.info("Shutting down Netty server...");
        shutdown = true;

        // 关闭业务线程池
        if (businessGroup != null) {
            businessGroup.shutdownGracefully(1, 15, TimeUnit.SECONDS);
        }

        // 关闭WorkerGroup
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(1, 15, TimeUnit.SECONDS);
        }

        // 关闭BossGroup
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(1, 15, TimeUnit.SECONDS);
        }

        // 关闭服务器Channel
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }

        // 重置服务器状态标志
        started = false;
        shutdown = false;

        logger.info("Netty server shut down successfully");
    }
}


package com.xx.jaseatschoicejava.config;

import com.xx.jaseatschoicejava.netty.NettyServer;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Netty与Spring集成配置
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
@Configuration
public class NettySpringConfig {

    private final NettyServer nettyServer;

    public NettySpringConfig(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    /**
     * 启动Netty服务器
     */
    @PostConstruct
    public void startNettyServer() {
        try {
            nettyServer.start();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to start Netty server", e);
        }
    }

    /**
     * 关闭Netty服务器
     */
    @PreDestroy
    public void stopNettyServer() {
        nettyServer.shutdown();
    }
}


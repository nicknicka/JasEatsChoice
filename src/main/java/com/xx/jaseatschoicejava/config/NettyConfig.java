package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Netty配置类
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
@Configuration
public class NettyConfig {

    /**
     * Netty服务器端口
     */
    @Value("${netty.port:9091}")
    private int port;

    /**
     * BossGroup线程数
     */
    @Value("${netty.bossGroup.threads:1}")
    private int bossGroupThreads;

    /**
     * WorkerGroup线程数
     */
    @Value("${netty.workerGroup.threads:8}")
    private int workerGroupThreads;

    /**
     * 最大帧长度
     */
    @Value("${netty.maxFrameLength:65536}")
    private int maxFrameLength;

    /**
     * 长度字段偏移量
     */
    @Value("${netty.lengthFieldOffset:0}")
    private int lengthFieldOffset;

    /**
     * 长度字段长度
     */
    @Value("${netty.lengthFieldLength:4}")
    private int lengthFieldLength;

    /**
     * 长度字段调整
     */
    @Value("${netty.lengthAdjustment:0}")
    private int lengthAdjustment;

    /**
     * 读取长度后跳过的字节数
     */
    @Value("${netty.initialBytesToStrip:4}")
    private int initialBytesToStrip;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossGroupThreads() {
        return bossGroupThreads;
    }

    public void setBossGroupThreads(int bossGroupThreads) {
        this.bossGroupThreads = bossGroupThreads;
    }

    public int getWorkerGroupThreads() {
        return workerGroupThreads;
    }

    public void setWorkerGroupThreads(int workerGroupThreads) {
        this.workerGroupThreads = workerGroupThreads;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public int getLengthFieldOffset() {
        return lengthFieldOffset;
    }

    public void setLengthFieldOffset(int lengthFieldOffset) {
        this.lengthFieldOffset = lengthFieldOffset;
    }

    public int getLengthFieldLength() {
        return lengthFieldLength;
    }

    public void setLengthFieldLength(int lengthFieldLength) {
        this.lengthFieldLength = lengthFieldLength;
    }

    public int getLengthAdjustment() {
        return lengthAdjustment;
    }

    public void setLengthAdjustment(int lengthAdjustment) {
        this.lengthAdjustment = lengthAdjustment;
    }

    public int getInitialBytesToStrip() {
        return initialBytesToStrip;
    }

    public void setInitialBytesToStrip(int initialBytesToStrip) {
        this.initialBytesToStrip = initialBytesToStrip;
    }
}


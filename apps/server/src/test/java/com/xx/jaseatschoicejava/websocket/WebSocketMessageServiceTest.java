package com.xx.jaseatschoicejava.websocket;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 在线连接注册表的对外契约。
 *
 * 接缝：注册表本身就是 seam —— 测试只用 registerChannel / pushToUser / broadcast 等
 * 公开方法，通过真实的 Netty 连接对象观察"消息是否真的写到了连接上"，
 * 不触碰内部结构，也不 mock Channel。
 */
public class WebSocketMessageServiceTest {

    private final WebSocketMessageService service = new WebSocketMessageService();
    private final List<EmbeddedChannel> openedChannels = new ArrayList<>();

    private EmbeddedChannel openChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        openedChannels.add(channel);
        return channel;
    }

    @After
    public void tearDown() {
        // 注册表是静态的，测试之间必须彻底断开，否则会互相污染
        for (EmbeddedChannel channel : openedChannels) {
            // 注册表是静态的：必须显式注销，否则会污染同 JVM 内的其它测试
            WebSocketMessageService.removeChannel(channel);
            if (channel.isOpen()) {
                channel.close();
            }
            channel.finishAndReleaseAll();
        }
        openedChannels.clear();
    }

    @Test
    public void pushDeliversToEveryConnectionOfTheSameUser() {
        EmbeddedChannel firstTab = openChannel();
        EmbeddedChannel secondTab = openChannel();

        WebSocketMessageService.registerChannel("user-1", firstTab);
        WebSocketMessageService.registerChannel("user-1", secondTab);

        int delivered = WebSocketMessageService.pushToUser("user-1", "{\"msgType\":\"recommendation\"}");

        assertEquals("同一用户的两条连接都应收到消息", 2, delivered);
        assertNotNull("先建立的连接不应被后建立的覆盖", readText(firstTab));
        assertNotNull("后建立的连接也应收到消息", readText(secondTab));
    }

    @Test
    public void pushToOfflineUserIsSkippedQuietly() {
        int delivered = WebSocketMessageService.pushToUser("nobody-online", "{\"msgType\":\"recommendation\"}");

        assertEquals("离线用户应返回 0，且不抛异常", 0, delivered);
    }

    @Test
    public void broadcastReachesEveryOnlineConnectionAcrossUsers() {
        EmbeddedChannel first = openChannel();
        EmbeddedChannel second = openChannel();
        EmbeddedChannel third = openChannel();

        WebSocketMessageService.registerChannel("user-1", first);
        WebSocketMessageService.registerChannel("user-1", second);
        WebSocketMessageService.registerChannel("user-2", third);

        int delivered = WebSocketMessageService.broadcastToAll("{\"msgType\":\"recommend_update\"}");

        assertEquals("广播应覆盖所有在线连接", 3, delivered);
        assertNotNull(readText(first));
        assertNotNull(readText(second));
        assertNotNull(readText(third));
    }

    @Test
    public void writeThatFailsImmediatelyIsNotCountedAsDelivered() {
        // 连接仍报告 active，但写入立刻失败（对端刚断开、事件循环拒绝写入）。
        // 这种"尝试了但没写进去"的连接不应被算作已送达。
        EmbeddedChannel failing = new EmbeddedChannel(new io.netty.channel.ChannelOutboundHandlerAdapter() {
            @Override
            public void write(io.netty.channel.ChannelHandlerContext ctx, Object msg,
                              io.netty.channel.ChannelPromise promise) {
                promise.setFailure(new java.io.IOException("模拟写入失败"));
            }
        });
        openedChannels.add(failing);
        WebSocketMessageService.registerChannel("user-write-fails", failing);

        int delivered = WebSocketMessageService.pushToUser("user-write-fails", "{\"msgType\":\"recommendation\"}");

        assertEquals("写入失败的连接不应计入送达", 0, delivered);
    }

    @Test
    public void onlineUsersSnapshotIsTakenAtCallTime() {
        EmbeddedChannel channel = openChannel();
        WebSocketMessageService.registerChannel("user-snapshot", channel);

        java.util.Set<String> snapshot = WebSocketMessageService.getOnlineUsers();
        assertTrue("在线用户快照应包含刚注册的用户", snapshot.contains("user-snapshot"));

        // 快照取得后再有变动，不应影响已返回的集合
        EmbeddedChannel late = openChannel();
        WebSocketMessageService.registerChannel("user-late", late);

        assertFalse("快照不应随后续注册而变化", snapshot.contains("user-late"));
    }

    @Test
    public void closedConnectionIsRemovedFromOnlineUsers() {
        EmbeddedChannel channel = openChannel();
        WebSocketMessageService.registerChannel("user-closing", channel);

        channel.close();
        WebSocketMessageService.removeChannel(channel);

        assertFalse("断开后不应仍在在线用户中", WebSocketMessageService.getOnlineUsers().contains("user-closing"));
        assertEquals("断开后推送应返回 0", 0,
                WebSocketMessageService.pushToUser("user-closing", "{\"msgType\":\"recommendation\"}"));
    }

    private String readText(EmbeddedChannel channel) {
        TextWebSocketFrame frame = channel.readOutbound();
        return frame == null ? null : frame.text();
    }
}

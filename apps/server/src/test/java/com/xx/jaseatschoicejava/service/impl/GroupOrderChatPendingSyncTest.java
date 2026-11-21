package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 商家待同步消息（#595）。
 *
 * <p>原实现直接 {@code return List.of()}，接口对外永远是空列表。
 * 这里验证按 toId = merchantId 且未同步的条件返回真实消息内容。
 */
class GroupOrderChatPendingSyncTest {

    private ChatMsg msg(String id, String content, Boolean synced) {
        ChatMsg msg = new ChatMsg();
        msg.setMsgId(id);
        msg.setFromId("user-1");
        msg.setToId("merchant-1");
        msg.setSessionId("S1");
        msg.setSessionType("group");
        msg.setMsgType("text");
        msg.setContent(content);
        msg.setSynced(synced);
        msg.setCreatedAt(LocalDateTime.of(2026, 11, 21, 10, 0));
        return msg;
    }

    /** @RequiredArgsConstructor 生成了 5 参构造器，其余依赖在本用例里用不到。 */
    private GroupOrderChatServiceImpl newService(ChatMsgService chatMsgService, List<ChatMsg> rows) {
        GroupOrderChatServiceImpl service = spy(new GroupOrderChatServiceImpl(
            mock(com.xx.jaseatschoicejava.service.ChatSessionService.class),
            mock(com.xx.jaseatschoicejava.service.GroupOrderService.class),
            chatMsgService,
            mock(com.xx.jaseatschoicejava.service.ContactService.class),
            mock(com.xx.jaseatschoicejava.websocket.WebSocketMessageService.class)));
        when(chatMsgService.list(any(Wrapper.class))).thenReturn(rows);
        return service;
    }

    @Test
    void returnsMessagesWithFullPayload() {
        ChatMsgService chatMsgService = mock(ChatMsgService.class);
        GroupOrderChatServiceImpl service =
            newService(chatMsgService, List.of(msg("m-1", "订单已打包", false)));

        List<Map<String, Object>> result = service.getPendingSyncMessages("merchant-1", null);

        assertEquals(1, result.size());
        Map<String, Object> item = result.get(0);
        assertEquals("m-1", item.get("msgId"));
        assertEquals("订单已打包", item.get("content"));
        assertEquals("user-1", item.get("fromId"));
        assertEquals("merchant-1", item.get("toId"));
        assertEquals("S1", item.get("sessionId"));
        assertEquals("group", item.get("sessionType"));
        assertEquals("text", item.get("msgType"));
    }

    @Test
    void blankMerchantIdReturnsEmptyWithoutQuerying() {
        ChatMsgService chatMsgService = mock(ChatMsgService.class);
        GroupOrderChatServiceImpl service = newService(chatMsgService, List.of());

        assertTrue(service.getPendingSyncMessages("  ", null).isEmpty());
        verify(chatMsgService, never()).list(any(Wrapper.class));
    }

    @Test
    void returnsEmptyWhenNothingPending() {
        GroupOrderChatServiceImpl service =
            newService(mock(ChatMsgService.class), List.of());

        assertTrue(service.getPendingSyncMessages("merchant-1", "group").isEmpty());
    }
}

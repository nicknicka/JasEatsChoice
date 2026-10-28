package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.entity.Contact;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.service.ContactService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupOrderChatServiceImplTest {

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private GroupOrderService groupOrderService;

    @Mock
    private ChatMsgService chatMsgService;

    @Mock
    private ContactService contactService;

    @Mock
    private WebSocketMessageService webSocketMessageService;

    @InjectMocks
    private GroupOrderChatServiceImpl groupOrderChatService;

    @Test
    void syncMessageToGroup_shouldSaveRealGroupMessageAndPushToMembers() {
        ChatSession session = new ChatSession();
        session.setRelatedOrderId("GO-1");
        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setId("GO-1");
        groupOrder.setGroupId("G-1");
        Contact member1 = member("member-1");
        Contact member2 = member("member-2");
        LambdaQueryChainWrapper<Contact> query = org.mockito.Mockito.mock(LambdaQueryChainWrapper.class);

        when(chatSessionService.getOne(any())).thenReturn(session);
        when(groupOrderService.getById("GO-1")).thenReturn(groupOrder);
        when(chatMsgService.save(any(ChatMsg.class))).thenReturn(true);
        when(chatSessionService.updateById(any(ChatSession.class))).thenReturn(true);
        when(contactService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(member1, member2));

        String result = groupOrderChatService.syncMessageToGroup(
            "merchant-1", "GO-1", "预计 10 分钟后出餐", "出餐进度");

        assertEquals("【订单同步】[出餐进度] 预计 10 分钟后出餐", result);

        ArgumentCaptor<ChatMsg> messageCaptor = ArgumentCaptor.forClass(ChatMsg.class);
        verify(chatMsgService).save(messageCaptor.capture());
        ChatMsg savedMessage = messageCaptor.getValue();
        assertEquals("merchant-1", savedMessage.getFromId());
        assertEquals("G-1", savedMessage.getToId());
        assertEquals(ChatSessionIdGenerator.getGroupChatSessionId("G-1"), savedMessage.getSessionId());
        assertEquals("group", savedMessage.getSessionType());
        assertEquals(result, savedMessage.getContent());
        assertFalse(savedMessage.getSynced());

        verify(webSocketMessageService).pushChatMessageToUser("member-1", savedMessage);
        verify(webSocketMessageService).pushChatMessageToUser("member-2", savedMessage);
    }

    @Test
    void markMessageAsSynced_shouldPersistSyncedState() {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setMsgId("CM-1");
        chatMsg.setSynced(false);
        when(chatMsgService.getById("CM-1")).thenReturn(chatMsg);
        when(chatMsgService.updateById(chatMsg)).thenReturn(true);

        boolean result = groupOrderChatService.markMessageAsSynced("CM-1");

        assertTrue(result);
        assertTrue(chatMsg.getSynced());
        verify(chatMsgService).updateById(chatMsg);
    }

    @Test
    void markMessageAsSynced_whenMessageMissing_shouldReturnFalse() {
        when(chatMsgService.getById("missing")).thenReturn(null);

        boolean result = groupOrderChatService.markMessageAsSynced("missing");

        assertFalse(result);
        verify(chatMsgService, never()).updateById(any(ChatMsg.class));
    }

    private Contact member(String userId) {
        Contact member = new Contact();
        member.setUserId(userId);
        return member;
    }
}

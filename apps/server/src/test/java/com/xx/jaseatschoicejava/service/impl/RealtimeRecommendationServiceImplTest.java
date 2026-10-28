package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.dto.RecommendationRequestDTO;
import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.service.RecommendationService;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 定时推荐推送的对外行为。
 *
 * 接缝：直接调用接口上的 scheduleRecommendationPush（不等待 30 分钟调度），
 * 用真实 Netty 连接观察"消息是否真的写到了在线用户的连接上"。
 */
@RunWith(MockitoJUnitRunner.class)
public class RealtimeRecommendationServiceImplTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private DishMapper dishMapper;

    @InjectMocks
    private RealtimeRecommendationServiceImpl service;

    private final List<EmbeddedChannel> openedChannels = new ArrayList<>();

    private EmbeddedChannel openChannel(String userId) {
        EmbeddedChannel channel = new EmbeddedChannel();
        openedChannels.add(channel);
        WebSocketMessageService.registerChannel(userId, channel);
        return channel;
    }

    @After
    public void tearDown() {
        for (EmbeddedChannel channel : openedChannels) {
            WebSocketMessageService.removeChannel(channel);
            if (channel.isOpen()) {
                channel.close();
            }
            channel.finishAndReleaseAll();
        }
        openedChannels.clear();
    }

    private String readText(EmbeddedChannel channel) {
        TextWebSocketFrame frame = channel.readOutbound();
        return frame == null ? null : frame.text();
    }

    @Test
    public void scheduledPushDeliversRecommendationToOnlineUser() {
        EmbeddedChannel channel = openChannel("user-1");
        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenReturn(List.of(new RecommendationResultDTO()));

        service.scheduleRecommendationPush();

        String payload = readText(channel);
        assertNotNull("在线用户应收到推荐推送", payload);
        assertTrue("推送内容应标记为实时推荐", payload.contains("realtime_recommend"));
        assertTrue("推送内容应带上接收者标识", payload.contains("user-1"));
    }

    @Test
    public void scheduledPushFinishesQuietlyWhenNobodyIsOnline() {
        // 无人在线：任务应正常结束，且完全不需要生成推荐内容
        service.scheduleRecommendationPush();

        verifyNoInteractions(recommendationService);
    }

    @Test
    public void deliveryCountReflectsRealDeliveriesNotAttempts() {
        openChannel("user-ok");

        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenReturn(List.of(new RecommendationResultDTO()));

        int delivered = service.deliverRealtimeRecommendation("user-ok", "定时推荐 - 午餐");

        assertTrue("成功送达应返回大于 0 的连接数", delivered > 0);
    }

    @Test
    public void deliveryCountIsZeroWhenTheUserIsOffline() {
        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenReturn(List.of(new RecommendationResultDTO()));

        int delivered = service.deliverRealtimeRecommendation("user-absent", "定时推荐 - 午餐");

        assertEquals("用户不在线时送达数应为 0", 0, delivered);
    }

    @Test
    public void deliveryCountReflectsTheBrokenUserAsNotDelivered() {
        openChannel("user-ok");

        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenAnswer(invocation -> {
                    RecommendationRequestDTO request = invocation.getArgument(0);
                    if ("user-broken".equals(request.getUserId())) {
                        throw new IllegalStateException("推荐服务对该用户不可用");
                    }
                    return List.of(new RecommendationResultDTO());
                });

        int delivered = service.deliverRealtimeRecommendation("user-broken", "定时推荐 - 午餐");

        assertEquals("失败用户不应被计入送达", 0, delivered);
    }

    @Test
    public void pushedMessageCarriesTheRecommendationPayload() {
        EmbeddedChannel channel = openChannel("user-payload");

        RecommendationResultDTO item = new RecommendationResultDTO();
        item.setDishId("dish-777");
        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenReturn(List.of(item));

        service.scheduleRecommendationPush();

        String payload = readText(channel);
        assertNotNull(payload);
        assertTrue("推送应携带真实推荐数据", payload.contains("dish-777"));
        assertTrue("推送应带上当前时段上下文", payload.contains("定时推荐"));
    }

    @Test
    public void offlineUserIsSkippedWithoutBlockingOthers() {
        EmbeddedChannel online = openChannel("user-online");
        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenReturn(List.of(new RecommendationResultDTO()));

        service.scheduleRecommendationPush();

        assertNotNull("在线用户应收到推送", readText(online));
    }

    @Test
    public void oneUserFailureDoesNotStopTheOthers() {
        EmbeddedChannel failingChannel = openChannel("user-broken");
        EmbeddedChannel healthyChannel = openChannel("user-healthy");

        when(recommendationService.getRecommendations(any(RecommendationRequestDTO.class)))
                .thenAnswer(invocation -> {
                    RecommendationRequestDTO request = invocation.getArgument(0);
                    if ("user-broken".equals(request.getUserId())) {
                        throw new IllegalStateException("推荐服务对该用户不可用");
                    }
                    return List.of(new RecommendationResultDTO());
                });

        service.scheduleRecommendationPush();

        assertNotNull("单个用户失败不应中断整批推送", readText(healthyChannel));
        assertTrue("失败的连接不应收到消息", failingChannel.outboundMessages().isEmpty());
    }
}

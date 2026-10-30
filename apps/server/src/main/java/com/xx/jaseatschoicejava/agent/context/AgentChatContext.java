package com.xx.jaseatschoicejava.agent.context;

import java.util.Objects;

/**
 * AI 聊天的可信运行上下文。
 *
 * userId 来自签名后的 JWT；merchantId 来自服务端用户绑定。
 * audience 与 merchantId 不接受聊天请求体或模型参数提供。
 */
public final class AgentChatContext {

    public static final String AUDIENCE_CUSTOMER = "customer";
    public static final String AUDIENCE_MERCHANT = "merchant";

    private final String userId;
    private final String audience;
    private final String merchantId;

    private AgentChatContext(String userId, String audience, String merchantId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("audience is required");
        }
        this.userId = userId;
        this.audience = audience;
        this.merchantId = merchantId;
    }

    public static AgentChatContext customer(String userId) {
        return new AgentChatContext(userId, AUDIENCE_CUSTOMER, null);
    }

    public static AgentChatContext merchant(String userId, String merchantId) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId is required for merchant audience");
        }
        return new AgentChatContext(userId, AUDIENCE_MERCHANT, merchantId);
    }

    public String getUserId() {
        return userId;
    }

    public String getAudience() {
        return audience;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public boolean isMerchant() {
        return AUDIENCE_MERCHANT.equals(audience);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AgentChatContext context)) {
            return false;
        }
        return userId.equals(context.userId)
                && audience.equals(context.audience)
                && Objects.equals(merchantId, context.merchantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, audience, merchantId);
    }
}

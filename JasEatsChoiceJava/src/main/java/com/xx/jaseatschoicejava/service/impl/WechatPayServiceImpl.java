package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.config.PaymentConfig;
import com.xx.jaseatschoicejava.service.WechatPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

/**
 * 微信支付服务实现
 * 注意：这是模拟实现，实际生产环境需要集成微信支付SDK
 * 参考文档：https://pay.weixin.qq.com/wiki/doc/apiv3/index.shtml
 */
@Service
public class WechatPayServiceImpl implements WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    private final PaymentConfig paymentConfig;

    private static final String PAY_PENDING = "pending";
    private static final String PAY_FAILED = "failed";

    public WechatPayServiceImpl(PaymentConfig paymentConfig) {
        this.paymentConfig = paymentConfig;
    }

    @Override
    public Map<String, Object> createMiniPayOrder(String paymentNo, BigDecimal amount, String description, String openid) {
        log.info("创建微信小程序支付订单，流水号：{}，金额：{}，描述：{}", paymentNo, amount, description);

        try {
            // 实际生产环境需要调用微信支付统一下单API
            // 这里返回模拟数据，前端可以正常调起支付（但会失败，因为没有真实的prepay_id）

            String timeStamp = String.valueOf(Instant.now().getEpochSecond());
            String nonceStr = generateNonceStr();
            String packageStr = "prepay_id=wx" + paymentNo.substring(3); // 模拟prepay_id
            String signType = "MD5";
            String paySign = generateSignature(timeStamp, nonceStr, packageStr, signType);

            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", paymentNo);
            result.put("status", PAY_PENDING);
            result.put("timeStamp", timeStamp);
            result.put("nonceStr", nonceStr);
            result.put("package", packageStr);
            result.put("signType", signType);
            result.put("paySign", paySign);
            result.put("message", "微信支付订单创建成功");

            log.info("微信小程序支付订单创建成功，流水号：{}", paymentNo);
            return result;

        } catch (Exception e) {
            log.error("创建微信小程序支付订单失败，流水号：{}", paymentNo, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("paymentNo", paymentNo);
            errorResult.put("status", PAY_FAILED);
            errorResult.put("message", "创建支付订单失败：" + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public String createH5PayOrder(String paymentNo, BigDecimal amount, String description) {
        log.info("创建微信H5支付订单，流水号：{}，金额：{}", paymentNo, amount);

        // 实际生产环境需要调用微信支付H5下单API
        // 返回H5支付URL
        String h5PayUrl = "https://wxpay.wxutil.com/midwap/pay?prepay_id=" + paymentNo;

        log.info("微信H5支付订单创建成功，URL：{}", h5PayUrl);
        return h5PayUrl;
    }

    @Override
    public Map<String, Object> queryOrder(String outTradeNo) {
        log.info("查询微信支付订单，商户订单号：{}", outTradeNo);

        // 实际生产环境需要调用微信支付查询订单API
        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("tradeState", "NOTPAY"); // 模拟状态：NOTPAY-未支付, SUCCESS-支付成功, CLOSED-已关闭, REVOKED-已撤销
        result.put("tradeStateDesc", "未支付");

        return result;
    }

    @Override
    public boolean closeOrder(String outTradeNo) {
        log.info("关闭微信支付订单，商户订单号：{}", outTradeNo);

        // 实际生产环境需要调用微信支付关闭订单API
        log.info("微信支付订单已关闭，商户订单号：{}", outTradeNo);
        return true;
    }

    @Override
    public Map<String, Object> handleNotification(Map<String, Object> notification) {
        log.info("处理微信支付回调，数据：{}", notification);

        try {
            // 验证签名
            String signature = (String) notification.get("signature");
            if (!verifySignature(notification, signature)) {
                log.warn("微信支付回调签名验证失败");
                Map<String, Object> failResult = new HashMap<>();
                failResult.put("code", "FAIL");
                failResult.put("message", "签名验证失败");
                return failResult;
            }

            // 处理支付成功逻辑
            String outTradeNo = (String) notification.get("out_trade_no");
            log.info("微信支付成功，商户订单号：{}", outTradeNo);

            Map<String, Object> successResult = new HashMap<>();
            successResult.put("code", "SUCCESS");
            successResult.put("message", "处理成功");
            return successResult;

        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            Map<String, Object> failResult = new HashMap<>();
            failResult.put("code", "FAIL");
            failResult.put("message", "处理失败：" + e.getMessage());
            return failResult;
        }
    }

    @Override
    public Map<String, Object> refund(String paymentNo, BigDecimal refundAmount, String reason) {
        log.info("微信支付退款，流水号：{}，退款金额：{}，原因：{}", paymentNo, refundAmount, reason);

        // 实际生产环境需要调用微信支付退款API
        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", paymentNo);
        result.put("refundStatus", "processing"); // processing-处理中, success-成功, failed-失败
        result.put("refundId", "RF" + System.currentTimeMillis());
        result.put("message", "退款申请已提交");

        log.info("微信支付退款申请成功，流水号：{}", paymentNo);
        return result;
    }

    @Override
    public Map<String, Object> queryRefund(String outTradeNo) {
        log.info("查询微信支付退款，商户订单号：{}", outTradeNo);

        // 实际生产环境需要调用微信支付查询退款API
        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("refundStatus", "success");
        result.put("refundAmount", BigDecimal.ZERO);

        return result;
    }

    @Override
    public boolean verifySignature(Map<String, Object> data, String signature) {
        // 实际生产环境需要使用微信支付平台证书验证签名
        // 这里简化处理，实际场景必须验证签名以确保请求来自微信
        log.debug("验证微信支付签名，signature：{}", signature);
        return true; // 模拟验证通过
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成签名
     * 注意：实际生产环境需要按照微信支付官方文档的签名算法实现
     */
    private String generateSignature(String timeStamp, String nonceStr, String packageStr, String signType) {
        try {
            // 模拟签名生成
            // 实际需要：appId + timeStamp + nonceStr + packageStr + key 进行MD5加密
            String apiKey = paymentConfig.getWechat().getApiKey();
            String appId = paymentConfig.getWechat().getAppId();

            // 构造签名字符串
            String signStr = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageStr + "\n";

            // MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((signStr + apiKey).getBytes("UTF-8"));

            // 转换为十六进制
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString().toUpperCase();

        } catch (Exception e) {
            log.error("生成签名失败", e);
            return ""; // 实际应该抛出异常
        }
    }
}

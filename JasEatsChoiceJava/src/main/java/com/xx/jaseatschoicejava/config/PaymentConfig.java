package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置类
 * 用于配置微信支付和支付宝支付的参数
 */
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {

    /**
     * 微信支付配置
     */
    private WechatConfig wechat = new WechatConfig();

    /**
     * 支付宝支付配置
     */
    private AlipayConfig alipay = new AlipayConfig();

    public WechatConfig getWechat() {
        return wechat;
    }

    public void setWechat(WechatConfig wechat) {
        this.wechat = wechat;
    }

    public AlipayConfig getAlipay() {
        return alipay;
    }

    public void setAlipay(AlipayConfig alipay) {
        this.alipay = alipay;
    }

    /**
     * 微信支付配置
     */
    public static class WechatConfig {
        /**
         * 应用ID（小程序AppID或公众号AppID）
         */
        private String appId;

        /**
         * 商户号
         */
        private String mchId;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API证书序列号
         */
        private String certSerialNo;

        /**
         * API证书路径
         */
        private String certPath;

        /**
         * API密钥路径
         */
        private String keyPath;

        /**
         * 通知URL
         */
        private String notifyUrl;

        /**
         * 是否启用沙箱环境
         */
        private boolean sandbox = false;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getCertSerialNo() {
            return certSerialNo;
        }

        public void setCertSerialNo(String certSerialNo) {
            this.certSerialNo = certSerialNo;
        }

        public String getCertPath() {
            return certPath;
        }

        public void setCertPath(String certPath) {
            this.certPath = certPath;
        }

        public String getKeyPath() {
            return keyPath;
        }

        public void setKeyPath(String keyPath) {
            this.keyPath = keyPath;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public boolean isSandbox() {
            return sandbox;
        }

        public void setSandbox(boolean sandbox) {
            this.sandbox = sandbox;
        }
    }

    /**
     * 支付宝支付配置
     */
    public static class AlipayConfig {
        /**
         * 应用ID
         */
        private String appId;

        /**
         * 应用私钥
         */
        private String appPrivateKey;

        /**
         * 支付宝公钥
         */
        private String alipayPublicKey;

        /**
         * 服务器异步通知URL
         */
        private String notifyUrl;

        /**
         * 是否启用沙箱环境
         */
        private boolean sandbox = false;

        /**
         * 签名类型（RSA2）
         */
        private String signType = "RSA2";

        /**
         * 字符编码
         */
        private String charset = "UTF-8";

        /**
         * 格式类型
         */
        private String format = "json";

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppPrivateKey() {
            return appPrivateKey;
        }

        public void setAppPrivateKey(String appPrivateKey) {
            this.appPrivateKey = appPrivateKey;
        }

        public String getAlipayPublicKey() {
            return alipayPublicKey;
        }

        public void setAlipayPublicKey(String alipayPublicKey) {
            this.alipayPublicKey = alipayPublicKey;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public boolean isSandbox() {
            return sandbox;
        }

        public void setSandbox(boolean sandbox) {
            this.sandbox = sandbox;
        }

        public String getSignType() {
            return signType;
        }

        public void setSignType(String signType) {
            this.signType = signType;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }
}

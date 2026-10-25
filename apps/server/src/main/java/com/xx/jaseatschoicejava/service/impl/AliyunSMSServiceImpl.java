package com.xx.jaseatschoicejava.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.xx.jaseatschoicejava.config.AliyunSMSProperties;
import com.xx.jaseatschoicejava.service.AliyunSMSService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.util.regex.Pattern;

/**
 * 阿里云短信认证服务实现
 */
@Service
public class AliyunSMSServiceImpl implements AliyunSMSService {

    @Resource
    private AliyunSMSProperties smsProperties;

    @Resource
    private Client aliyunSmsClient;

    // 手机号正则（简单校验）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public void sendSmsVerifyCode(String phone, String code) throws Exception {
        // 1. 校验手机号
        Assert.isTrue(PHONE_PATTERN.matcher(phone).matches(), "手机号格式错误！");
        // 2. 校验验证码（假设6位数字）
        Assert.isTrue(code.matches("^\\d{6}$"), "验证码必须为6位数字！");

        // 3. 模拟模式判断
        if (Boolean.TRUE.equals(smsProperties.getMockMode())) {
            // 模拟模式：直接打印到控制台，不真实发送
            System.out.println("========== 短信模拟发送 ==========");
            System.out.println("手机号：" + phone);
            System.out.println("验证码：" + code);
            System.out.println("有效期：" + smsProperties.getCodeExpireMinutes() + " 分钟");
            System.out.println("签名：" + smsProperties.getSignName());
            System.out.println("模板CODE：" + smsProperties.getTemplateCode());
            System.out.println("================================");
            return;
        }

        // 4. 真实发送模式：构建短信请求参数（模板参数必须含code和min）
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setSignName(smsProperties.getSignName()) // 签名
                .setTemplateCode(smsProperties.getTemplateCode()) // 模板CODE
                .setPhoneNumber(phone) // 手机号
                // 模板参数：JSON格式，必须包含"code"（验证码）和"min"（有效期）
                .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"" + smsProperties.getCodeExpireMinutes() + "\"}");

        // 5. 调用接口发送短信（捕获异常可自定义处理）
        try {
            aliyunSmsClient.sendSmsVerifyCode(request);
            System.out.println("短信发送成功！手机号：" + phone + "，验证码：" + code);
        } catch (Exception e) {
            System.err.println("短信发送失败！原因：" + e.getMessage());
            throw new Exception("短信发送失败，请稍后重试", e);
        }
    }
}
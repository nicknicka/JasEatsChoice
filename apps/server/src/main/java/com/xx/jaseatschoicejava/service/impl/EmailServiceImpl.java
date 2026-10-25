package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.config.EmailProperties;
import com.xx.jaseatschoicejava.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 邮件发送服务实现
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private EmailProperties emailProperties;

    // 邮箱正则（简单校验）
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 获取邮件会话
     */
    private Session getMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", emailProperties.getHost());
        props.put("mail.smtp.port", emailProperties.getPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", emailProperties.isSslEnable());
        props.put("mail.smtp.starttls.enable", emailProperties.isStarttlsEnable());

        // 创建会话
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailProperties.getUsername(), emailProperties.getPassword());
            }
        });
    }

    @Override
    public void sendEmailVerifyCode(String email, String code) throws Exception {
        // 1. 校验邮箱
        Assert.isTrue(EMAIL_PATTERN.matcher(email).matches(), "邮箱格式错误！");
        // 2. 校验验证码（假设6位数字）
        Assert.isTrue(code.matches("^\\d{6}$"), "验证码必须为6位数字！");

        // 3. 构建邮件内容
        String subject = "【佳食宜选】邮箱验证码";
        String content = String.format(
                "尊敬的用户：\n\n" +
                        "您好！您正在进行邮箱验证操作，验证码为：%s\n\n" +
                        "验证码有效期为5分钟，请尽快完成验证。\n\n" +
                        "如非本人操作，请忽略此邮件。\n\n" +
                        "佳食宜选团队",
                code
        );

        // 4. 发送邮件
        sendTextEmail(email, subject, content);
        log.info("验证码邮件发送成功！收件人：{}，验证码：{}", email, code);
    }

    @Override
    public void sendTextEmail(String to, String subject, String content) throws Exception {
        // 1. 校验邮箱
        Assert.isTrue(EMAIL_PATTERN.matcher(to).matches(), "邮箱格式错误！");
        Assert.hasText(subject, "邮件主题不能为空");
        Assert.hasText(content, "邮件内容不能为空");

        // 2. 创建邮件消息
        Session session = getMailSession();
        MimeMessage message = new MimeMessage(session);

        try {
            // 设置发件人
            message.setFrom(new InternetAddress(emailProperties.getUsername()));
            // 设置收件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            // 设置主题
            message.setSubject(subject, "UTF-8");
            // 设置内容
            message.setText(content, "UTF-8");
            // 设置发送时间
            message.setSentDate(new java.util.Date());

            // 3. 发送邮件
            Transport.send(message);
            log.info("邮件发送成功！收件人：{}，主题：{}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败！收件人：{}，原因：{}", to, e.getMessage());
            throw new Exception("邮件发送失败，请稍后重试", e);
        }
    }
}

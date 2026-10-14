package cn.miskk.blog.user.service;

import cn.miskk.blog.common.exception.BizException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 邮件服务：配置了 spring.mail.host 时发送真实邮件；
 * 未配置（本地开发）时仅打印日志，注册验证码可直接在响应中返回（miskk.mail.debug=true）。
 */
@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.username:noreply@miskk.blog}")
    private String from;

    public void sendVerifyCode(String to, String code) {
        String subject = "迷失客客の博客 - 注册验证码";
        String content = """
                <div style="padding:24px;font-family:sans-serif;">
                  <h2 style="color:#e8639a;">喵~ 欢迎来到迷失客客の博客</h2>
                  <p>你的注册验证码是：</p>
                  <p style="font-size:28px;font-weight:bold;letter-spacing:6px;color:#e8639a;">%s</p>
                  <p style="color:#999;">验证码 5 分钟内有效，如果不是本人操作请忽略。</p>
                </div>
                """.formatted(code);

        // host 为空（本地开发）时 Spring 也会创建 JavaMailSender Bean，必须一并判断
        if (mailSender == null || host == null || host.isBlank()) {
            log.info("[DEV] 未配置邮件服务，验证码未真实发送: to={} code={}", to, code);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("验证码邮件发送失败: to={}", to, e);
            throw new BizException("邮件发送失败，请稍后再试");
        }
    }
}

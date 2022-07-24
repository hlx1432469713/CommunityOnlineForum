package com.hlx.communityonlineforum.Until;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


/**
 * 用于发送邮件
 */
@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;//邮件的发送者

    /**
     * 发送邮件
     * @param to ： 邮件的接收者
     * @param subject：邮件主题
     * @param content：邮件内容
     */
    public void sendMail(String to,String subject,String content){
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content,true);
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            logger.info("邮件发送成功！");
        } catch (MessagingException e) {
            logger.error("发送邮件失败 : " + e.getMessage());
        }
    }
}

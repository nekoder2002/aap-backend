package com.dhu.utils;

import com.dhu.exception.MailException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

@Component
public class EmailHelper {
    //发送邮件的主机
    @Value("${email.email-host}")
    private String emailHost;
    //邮件发送的协议
    @Value("${email.transport-type}")
    private String transportType;
    //发件人名称
    @Value("${email.from-user}")
    private String fromUser;
    //发件人邮箱
    @Value("${email.from-email}")
    private String fromEmail;
    //发件人邮箱授权码
    @Value("${email.auth-code}")
    private String authCode;

    private Properties props;

    @PostConstruct
    public void init(){
        props = new Properties();
        props.setProperty("mail.transport.protocol", transportType);
        props.setProperty("mail.host", emailHost);
        props.setProperty("mail.user", fromUser);
        props.setProperty("mail.from", fromEmail);
    }

    // 校验邮箱地址是否合法
    public boolean verifyEmail(String email){
        return Pattern.matches("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\\\.[a-zA-Z0-9-]+)*\\\\.[a-zA-Z0-9]{2,6}$",email);
    }

    //生成验证码
    public String generateCaptcha(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    builder.append((char)('a' + random.nextInt(26)));
                } else {
                    builder.append((char)('A' + random.nextInt(26)));
                }
            } else {
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    //发送邮件
    public void sendMessage(String toEmail, String subject, String content) {
        //获取Session对象
        Session session = Session.getInstance(props, null);
        // 3：创建MimeMessage对象
        MimeMessage message = new MimeMessage(session);
        Transport transport = null;
        try {
            // 设置收件人：
            InternetAddress to = new InternetAddress(toEmail);
            message.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件主题
            message.setSubject(subject);
            //设置邮件内容,这里我使用html格式，其实也可以使用纯文本；纯文本"text/plain"
            message.setContent(content, "text/html;charset=UTF-8");
            // 保存上面设置的邮件内容
            message.saveChanges();
            // 获取Transport对象
            transport = session.getTransport();
            // smtp验证，就是你用来发邮件的邮箱用户名密码（若在之前的properties中指定默认值，这里可以不用再次设置）
            transport.connect(null, null, authCode);
            // 发送邮件
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            throw new MailException("Failed to send message to <" + toEmail + '>');
        }finally {
            if (transport!=null){
                try {
                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

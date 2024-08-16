package com.elara.authorizationservice.service;

import com.elara.authorizationservice.dto.request.NotificationRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Objects;

@Slf4j
@Service
public class MailService {

    @Value("${app.mail.sender}")
    private String sender;

    @Autowired
    JavaMailSender emailSender;

    public void sendNotification(NotificationRequest notification) {
        if (StringUtils.hasText(notification.getAttachment())) {
            sendMailWithAttachment(notification);
        } else {
            sendMail(notification);
        }
    }

    private void sendMail(NotificationRequest notification) {
        try {
            log.info("Email sender: {}", notification.getSenderEmail());
            log.info("Email recipient: {}", notification.getRecipientEmail());
            MimeMessage message = emailSender.createMimeMessage();
            message.setRecipients(MimeMessage.RecipientType.TO, notification.getRecipientEmail());
            message.setFrom(new InternetAddress(notification.getSenderEmail() == null || notification.getSenderEmail().trim().isEmpty() ? sender : notification.getSenderEmail()));
            message.setSubject(notification.getSubject());
            message.setContent(notification.getHtml() != null ? notification.getHtml() : notification.getMessage(), "text/html; charset=utf-8");
            emailSender.send(message);
        } catch (MessagingException e) {
            log.error("error sending mail: ", e);
        }
    }

    private void sendMailWithAttachment(NotificationRequest notification) {
        // Creating a Mime Message
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            // Setting multipart as true for attachment to be send
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(new InternetAddress(notification.getSenderEmail() == null || notification.getSenderEmail().trim().isEmpty() ? sender : notification.getSenderEmail()));
            mimeMessageHelper.setTo(notification.getRecipientEmail());
            mimeMessageHelper.setSubject(notification.getSubject());
            mimeMessageHelper.setText(notification.getHtml(), true);

            if (notification.getAttachment().startsWith("http")) {
                byte[] fileBytes = downloadFileFromS3(notification.getAttachment());
                ByteArrayResource fileResource = new ByteArrayResource(fileBytes);
                mimeMessageHelper.addAttachment("Account Statement", fileResource);
            } else {
                FileSystemResource file = new FileSystemResource(new File(notification.getAttachment()));
                mimeMessageHelper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
            }
            emailSender.send(mimeMessage);
        }
        catch (MessagingException e) {

            log.error("Error while sending email: ", e);
        }
    }

    private byte[] downloadFileFromS3(String s3Link) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(s3Link, byte[].class);
    }

}
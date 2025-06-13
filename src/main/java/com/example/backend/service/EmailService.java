package com.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationEmail(String toEmail, String verificationCode, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("email", toEmail);
            context.setVariable("verificationCode", verificationCode);
            context.setVariable("verificationUrl", baseUrl + "/verify?token=" + verificationCode);

            String htmlContent = templateEngine.process("email/verification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email - CV Generator");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendCVCreatedNotification(String toEmail, String firstName, String cvTitle, Long cvId) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("cvTitle", cvTitle);
            context.setVariable("downloadUrl", baseUrl + "/cv/" + cvId + "/download");
            context.setVariable("viewUrl", baseUrl + "/cv/" + cvId);

            String htmlContent = templateEngine.process("email/cv-created", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your CV has been created - CV Generator");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("CV creation notification sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send CV creation notification to: {}", toEmail, e);
            // Don't throw exception here as CV creation should not fail due to email issues
            logger.warn("CV creation proceeded despite email notification failure");
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("resetUrl", baseUrl + "/reset-password?token=" + resetToken);

            String htmlContent = templateEngine.process("email/password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - CV Generator");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
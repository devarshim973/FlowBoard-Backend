package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.mail.from.email:}")
    private String senderEmail;

    @Value("${app.mail.from.name:FlowBoard}")
    private String senderName;

    @Value("${admin.verification.mail:}")
    private String adminVerifcationMail;

    private final JavaMailSender mailSender;

    public void send(String toEmail, String subject, String htmlContent) {
        if (smtpUsername == null || smtpUsername.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            log.warn("SMTP email is not configured. Skipping email send for {}", toEmail);
            throw new OtpException("SMTP email is not configured. Set SMTP_USERNAME, SMTP_APP_PASSWORD, and SMTP_FROM_EMAIL before sending OTP emails.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("SMTP mail sent successfully to {}", toEmail);
        } catch (MailAuthenticationException e) {
            log.error("SMTP authentication error: {}", e.getMessage());
            throw new OtpException("SMTP authentication failed. Check SMTP_USERNAME and SMTP_APP_PASSWORD.");
        } catch (MessagingException e) {
            log.error("SMTP message build error: {}", e.getMessage());
            throw new OtpException("SMTP message could not be created. Check sender and recipient email values.");
        } catch (MailException e) {
            log.error("SMTP mail error: {}", e.getMessage());
            throw new OtpException("Unable to send email via SMTP. Check Gmail SMTP settings and the app password.");
        } catch (Exception e) {
            log.error("SMTP unexpected error: {}", e.getMessage());
            throw new OtpException("Unexpected SMTP error while sending email.");
        }
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp){

        String htmlContent = """        
        <html>
          <body style="font-family: Arial, sans-serif; color: #333;">
            <h2>Password Reset OTP</h2>
            <p>Hello,</p>
            <p>Use the following OTP to reset your FlowBoard account password:</p>
            <h3 style="color: #007bff;">%s</h3>
            <p>This OTP will expire in 5 minutes.</p>
            <hr>
            <p style="font-size:12px;color:gray;">
              If you did not request this, you can safely ignore this email.
            </p>
          </body>
        </html>
        """.formatted(otp);

        this.send(toEmail, "FlowBoard - Password Reset OTP", htmlContent);
    }

    @Override
    public void sendSignupOtpEmail(String toEmail, String otp) {
        String htmlContent = """
        <html>
          <body style="font-family: Arial, sans-serif; color: #333;">
            <h2>Signup Verification OTP</h2>
            <p>Hello,</p>
            <p>Use the following OTP to verify your email and complete your FlowBoard registration:</p>
            <h3 style="color: #0f766e;">%s</h3>
            <p>This OTP will expire in 5 minutes.</p>
            <hr>
            <p style="font-size:12px;color:gray;">
              If you did not request this, you can safely ignore this email.
            </p>
          </body>
        </html>
        """.formatted(otp);

        this.send(toEmail, "FlowBoard - Signup Verification OTP", htmlContent);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        String subject = "FlowBoard - Verify Your Email Address";

        String htmlContent = """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2>Email Verification</h2>
        <p>Hello,</p>
        <p>Thank you for signing up with <b>FlowBoard</b>.</p>
        <p>Please click the button below to verify your email address:</p>
        <p>
          <a href="%s" 
             style="display:inline-block; padding:10px 20px; color:white; background-color:#007bff;
                    text-decoration:none; border-radius:5px;">
             Verify Email
          </a>
        </p>
        <p>If the button above does not work, copy and paste the following link into your browser:</p>
        <p style="color:#007bff;">%s</p>
        <hr>
        <p style="font-size:12px;color:gray;">
          If you did not create this account, you can safely ignore this email.
        </p>
      </body>
    </html>
    """.formatted(verificationLink, verificationLink);

        this.send(toEmail, subject, htmlContent);
    }

    @Override
    public void sendVerificationEmailForAdmin(String toEmail, String verificationLink) {
        if (adminVerifcationMail == null || adminVerifcationMail.isBlank()) {
            log.warn("Admin verification email is not configured. Skipping admin verification send for {}", toEmail);
            return;
        }

        String subject = "FlowBoard - Verify Admin Email access";
        log.info(adminVerifcationMail);

        String htmlContent = """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2>Email Verification</h2>
        <p>Hello,</p>
        <p>This is admin access email, for email "%s"<b>FlowBoard</b>.</p>
        <p>Please click the button below to verify your email address:</p>
        <p>
          <a href="%s" 
             style="display:inline-block; padding:10px 20px; color:white; background-color:#007bff;
                    text-decoration:none; border-radius:5px;">
             Verify Email
          </a>
        </p>
        <p>If the button above does not work, copy and paste the following link into your browser:</p>
        <p style="color:#007bff;">%s</p>
        <hr>
        <p style="font-size:12px;color:gray;">
          Please confirm the email before allowing the email access.
        </p>
      </body>
    </html>
    """.formatted(toEmail, verificationLink, verificationLink);

        this.send(adminVerifcationMail, subject, htmlContent);
    }

    @Override
    public void sendAccountActivationMail(String email) {
        String subject = "FlowBoard - Account Activated";
        log.info("Sending account activation mail to {}", email);

        String htmlContent = """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2>Account Activated</h2>
        <p>Hello,</p>
        <p>Your <b>FlowBoard</b> account for email <b>%s</b> has been activated successfully.</p>
        <p>You can now login and continue using the platform.</p>
        <hr>
        <p style="font-size:12px;color:gray;">
          If you were not expecting this change, please contact the administrator.
        </p>
      </body>
    </html>
    """.formatted(email);

        this.send(email, subject, htmlContent);
    }

    @Override
    public void sendAccountDeactivatedMail(String email) {
        String subject = "FlowBoard - Account Deactivated";
        log.info("Sending account deactivation mail to {}", email);

        String htmlContent = """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2>Account Deactivated</h2>
        <p>Hello,</p>
        <p>Your <b>FlowBoard</b> account for email <b>%s</b> has been deactivated by the administrator.</p>
        <p>You will not be able to login until the account is activated again.</p>
        <hr>
        <p style="font-size:12px;color:gray;">
          If you believe this was done in error, please contact the administrator.
        </p>
      </body>
    </html>
    """.formatted(email);

        this.send(email, subject, htmlContent);
    }
}

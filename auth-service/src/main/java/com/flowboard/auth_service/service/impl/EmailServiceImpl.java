package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.service.EmailService;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.sender.email:}")
    private String senderEmail;

    @Value("${brevo.sender.name:FlowBoard}")
    private String senderName;

    @Value("${brevo.enabled:true}")
    private boolean brevoEnabled;

    @Value("${mail.transport.prefer-smtp:false}")
    private boolean preferSmtp;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${smtp-auth:true}")
    private boolean smtpAuth;

    @Value("${smtp-starttls-enable:true}")
    private boolean smtpStartTlsEnable;

    @Value("${admin.verification.mail:}")
    private String adminVerifcationMail;

    private final RestTemplate restTemplate;
    private final JavaMailSender javaMailSender;

    public void send(String toEmail, String subject, String htmlContent) {
        if (preferSmtp) {
            if (trySendViaSmtp(toEmail, subject, htmlContent)) {
                return;
            }

            if (trySendViaBrevo(toEmail, subject, htmlContent)) {
                return;
            }
        } else {
            if (trySendViaBrevo(toEmail, subject, htmlContent)) {
                return;
            }

            if (trySendViaSmtp(toEmail, subject, htmlContent)) {
                return;
            }
        }

        throw new IllegalStateException("Unable to send email. Configure Brevo or SMTP correctly.");
    }

    private boolean trySendViaBrevo(String toEmail, String subject, String htmlContent) {
        if (apiKey != null && apiKey.startsWith("xsmtpsib-")) {
            log.info("Brevo credential for {} looks like an SMTP key, skipping Brevo REST API and using SMTP fallback", toEmail);
            return false;
        }

        if (!brevoEnabled || apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            log.warn("Brevo email is not configured for {}", toEmail);
            return false;
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> body = new HashMap<>();

        body.put("sender", Map.of(
                "email", senderEmail,
                "name", senderName
        ));

        body.put("to", List.of(
                Map.of("email", toEmail)
        ));

        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            log.info("Brevo Status: {}", response.getStatusCode());
            log.info("Brevo Body: {}", response.getBody());
            return true;
        } catch (Exception e) {
            log.error("Brevo Error: {}", e.getMessage());
            return false;
        }
    }

    private boolean trySendViaSmtp(String toEmail, String subject, String htmlContent) {
        String resolvedHost = smtpHost;
        String resolvedUsername = smtpUsername;
        String resolvedPassword = smtpPassword;

        if ((resolvedHost == null || resolvedHost.isBlank() || resolvedUsername == null || resolvedUsername.isBlank() || resolvedPassword == null || resolvedPassword.isBlank())
                && apiKey != null && apiKey.startsWith("xsmtpsib-")
                && senderEmail != null && !senderEmail.isBlank()) {
            resolvedHost = "smtp-relay.brevo.com";
            resolvedUsername = senderEmail;
            resolvedPassword = apiKey;
        }

        if (resolvedHost == null || resolvedHost.isBlank() || resolvedUsername == null || resolvedUsername.isBlank() || resolvedPassword == null || resolvedPassword.isBlank()) {
            log.warn("SMTP is not configured for {}", toEmail);
            return false;
        }

        try {
            JavaMailSender mailSender = buildMailSender(resolvedHost, resolvedUsername, resolvedPassword);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(senderEmail != null && !senderEmail.isBlank() ? senderEmail : resolvedUsername, senderName);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("SMTP email sent successfully to {}", toEmail);
            return true;
        } catch (AuthenticationFailedException | MailAuthenticationException ex) {
            log.error("SMTP Error: {}", ex.getMessage());
            throw new IllegalStateException("SMTP authentication failed. Configure smtp-username with your Brevo SMTP login email and smtp-password with your Brevo SMTP key.");
        } catch (Exception ex) {
            log.error("SMTP Error: {}", ex.getMessage());
            return false;
        }
    }

    private JavaMailSender buildMailSender(String host, String username, String password) {
        if (smtpHost != null && !smtpHost.isBlank()
                && smtpUsername != null && !smtpUsername.isBlank()
                && smtpPassword != null && !smtpPassword.isBlank()) {
            return javaMailSender;
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(smtpStartTlsEnable));
        props.put("mail.debug", "false");
        return mailSender;
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

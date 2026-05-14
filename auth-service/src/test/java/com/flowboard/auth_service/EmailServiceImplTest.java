package com.flowboard.auth_service;

import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailServiceImpl(mailSender);
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

        ReflectionTestUtils.setField(emailService, "smtpUsername", "smtp-user");
        ReflectionTestUtils.setField(emailService, "senderEmail", "noreply@flowboard.com");
        ReflectionTestUtils.setField(emailService, "senderName", "FlowBoard");
        ReflectionTestUtils.setField(emailService, "adminVerifcationMail", "admin@flowboard.com");
    }

    @Test
    void send_withMissingSmtpConfig_throwsException() {
        ReflectionTestUtils.setField(emailService, "smtpUsername", " ");

        OtpException exception = assertThrows(OtpException.class,
                () -> emailService.send("user@flowboard.com", "subject", "<p>html</p>"));

        assertEquals("SMTP email is not configured. Set SMTP_USERNAME, SMTP_APP_PASSWORD, and SMTP_FROM_EMAIL before sending OTP emails.", exception.getMessage());
    }

    @Test
    void send_withAuthenticationError_throwsException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailAuthenticationException("bad auth")).when(mailSender).send(mimeMessage);

        OtpException exception = assertThrows(OtpException.class,
                () -> emailService.send("user@flowboard.com", "subject", "<p>html</p>"));

        assertEquals("SMTP authentication failed. Check SMTP_USERNAME and SMTP_APP_PASSWORD.", exception.getMessage());
    }

    @Test
    void send_withMailSendError_throwsException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("send failed")).when(mailSender).send(mimeMessage);

        OtpException exception = assertThrows(OtpException.class,
                () -> emailService.send("user@flowboard.com", "subject", "<p>html</p>"));

        assertEquals("Unable to send email via SMTP. Check Gmail SMTP settings and the app password.", exception.getMessage());
    }

    @Test
    void send_withUnexpectedError_throwsException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("boom"));

        OtpException exception = assertThrows(OtpException.class,
                () -> emailService.send("user@flowboard.com", "subject", "<p>html</p>"));

        assertEquals("Unexpected SMTP error while sending email.", exception.getMessage());
    }

    @Test
    void send_withValidConfig_sendsMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.send("user@flowboard.com", "subject", "<p>html</p>"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendOtpEmail_buildsAndSendsTemplate() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendOtpEmail("user@flowboard.com", "123456"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendSignupOtpEmail_buildsAndSendsTemplate() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendSignupOtpEmail("user@flowboard.com", "ABC123"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVerificationEmail_buildsAndSendsTemplate() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("user@flowboard.com", "https://flowboard.test/verify"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVerificationEmailForAdmin_withMissingAdminMailbox_skipsSend() {
        ReflectionTestUtils.setField(emailService, "adminVerifcationMail", " ");

        assertDoesNotThrow(() -> emailService.sendVerificationEmailForAdmin("user@flowboard.com", "https://flowboard.test/verify"));
    }

    @Test
    void sendVerificationEmailForAdmin_withConfiguredMailbox_sendsMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendVerificationEmailForAdmin("user@flowboard.com", "https://flowboard.test/verify"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void accountActivationAndDeactivationEmails_sendMessages() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendAccountActivationMail("user@flowboard.com"));
        assertDoesNotThrow(() -> emailService.sendAccountDeactivatedMail("user@flowboard.com"));
    }
}

package com.flowboard.notification_service;

import com.flowboard.notification_service.entity.Notification;
import com.flowboard.notification_service.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EmailServiceImpl(restTemplate);
        ReflectionTestUtils.setField(service, "apiKey", "brevo-key");
        ReflectionTestUtils.setField(service, "senderEmail", "noreply@flowboard.com");
        ReflectionTestUtils.setField(service, "senderName", "FlowBoard");
    }

    @Test
    void send_withMissingConfig_skipsRequest() {
        ReflectionTestUtils.setField(service, "apiKey", " ");

        assertDoesNotThrow(() -> service.send("user@flowboard.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void send_withValidConfig_postsToBrevo() {
        when(restTemplate.postForEntity(eq("https://api.brevo.com/v3/smtp/email"), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        service.send("user@flowboard.com", "Subject", "<p>Body</p>");

        verify(restTemplate).postForEntity(eq("https://api.brevo.com/v3/smtp/email"), any(), eq(String.class));
    }

    @Test
    void send_whenBrevoFails_swallowsException() {
        when(restTemplate.postForEntity(eq("https://api.brevo.com/v3/smtp/email"), any(), eq(String.class)))
                .thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> service.send("user@flowboard.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void templateMethods_delegateToSend() {
        Notification notification = new Notification();
        notification.setTitle("Task title");
        notification.setMessage("Task message");

        when(restTemplate.postForEntity(eq("https://api.brevo.com/v3/smtp/email"), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        service.sendAssignmentEmail("user@flowboard.com", notification);
        service.sendMentionEmail("user@flowboard.com", notification);
        service.sendDueDateEmail("user@flowboard.com", notification);

        verify(restTemplate, org.mockito.Mockito.times(3))
                .postForEntity(eq("https://api.brevo.com/v3/smtp/email"), any(), eq(String.class));
    }
}

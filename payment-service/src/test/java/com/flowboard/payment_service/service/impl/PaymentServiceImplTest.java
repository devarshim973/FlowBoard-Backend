package com.flowboard.payment_service.service.impl;

import com.flowboard.payment_service.dto.CreateOrderResponseDto;
import com.flowboard.payment_service.dto.SubscriptionStatusDto;
import com.flowboard.payment_service.dto.VerifyPaymentRequestDto;
import com.flowboard.payment_service.entity.UserSubscription;
import com.flowboard.payment_service.exception.PaymentException;
import com.flowboard.payment_service.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(restTemplate, userSubscriptionRepository);
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_live_flowboard");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "secret-value");
        ReflectionTestUtils.setField(paymentService, "currency", "INR");
        ReflectionTestUtils.setField(paymentService, "workspaceUpgradeAmount", 99900);
        ReflectionTestUtils.setField(paymentService, "workspaceUpgradeName", "FlowBoard Pro");
    }

    @Test
    void createOrderShouldRejectAlreadySubscribedUsers() {
        when(userSubscriptionRepository.findByUserId(1)).thenReturn(Optional.of(subscription(1, true, "existing-order")));

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.createOrder(1));

        assertEquals("You already have an active subscription", exception.getMessage());
    }

    @Test
    void createOrderShouldRejectPlaceholderConfiguration() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_flowboard");
        when(userSubscriptionRepository.findByUserId(2)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.createOrder(2));

        assertEquals("Razorpay keys are not configured. Add razorpay_key_id and razorpay_key_secret before taking payment.", exception.getMessage());
    }

    @Test
    void createOrderShouldWrapGatewayFailure() {
        when(userSubscriptionRepository.findByUserId(3)).thenReturn(Optional.empty());
        when(restTemplate.postForObject(eq("https://api.razorpay.com/v1/orders"), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("gateway down"));

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.createOrder(3));

        assertEquals("Unable to create Razorpay order right now. Check Razorpay key id/secret and internet access from payment-service.", exception.getMessage());
    }

    @Test
    void createOrderShouldRejectEmptyGatewayResponse() {
        when(userSubscriptionRepository.findByUserId(4)).thenReturn(Optional.empty());
        when(restTemplate.postForObject(eq("https://api.razorpay.com/v1/orders"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(Map.of());

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.createOrder(4));

        assertEquals("Razorpay order response was empty", exception.getMessage());
    }

    @Test
    void createOrderShouldUpdateExistingSubscriptionDraft() {
        UserSubscription subscription = subscription(5, false, "old-order");
        when(userSubscriptionRepository.findByUserId(5)).thenReturn(Optional.of(subscription));
        when(restTemplate.postForObject(eq("https://api.razorpay.com/v1/orders"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(Map.of("id", "order_123"));
        when(userSubscriptionRepository.save(subscription)).thenReturn(subscription);

        CreateOrderResponseDto response = paymentService.createOrder(5);

        assertEquals("order_123", response.getOrderId());
        assertEquals("rzp_live_flowboard", response.getKey());
        assertEquals(99900, response.getAmount());
        assertEquals("FlowBoard Pro", subscription.getPlanName());
        assertEquals("INR", subscription.getCurrency());
        assertFalse(subscription.getActive());
        verify(userSubscriptionRepository).save(subscription);
    }

    @Test
    void createOrderShouldCreateDraftSubscriptionWhenMissing() {
        when(userSubscriptionRepository.findByUserId(6)).thenReturn(Optional.empty());
        when(restTemplate.postForObject(eq("https://api.razorpay.com/v1/orders"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(Map.of("id", "order_456"));

        CreateOrderResponseDto response = paymentService.createOrder(6);

        assertEquals("order_456", response.getOrderId());
        verify(userSubscriptionRepository).save(any(UserSubscription.class));
    }

    @Test
    void verifyPaymentShouldRejectUnknownOrder() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto("order_x", "pay_x", "sig_x");
        when(userSubscriptionRepository.findByUserIdAndRazorpayOrderId(7, "order_x")).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.verifyPayment(7, request));

        assertEquals("Subscription order not found", exception.getMessage());
    }

    @Test
    void verifyPaymentShouldRejectInvalidSignature() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto("order_good", "pay_good", "bad_sig");
        when(userSubscriptionRepository.findByUserIdAndRazorpayOrderId(8, "order_good"))
                .thenReturn(Optional.of(subscription(8, false, "order_good")));

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.verifyPayment(8, request));

        assertEquals("Razorpay signature verification failed", exception.getMessage());
    }

    @Test
    void verifyPaymentShouldActivateSubscriptionWhenSignatureMatches() {
        UserSubscription subscription = subscription(9, false, "order_real");
        String signature = "7bcc50a18052321043dc791b22036d1ddf2a205072ae75ba4c45aefe9150c33f";
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto("order_real", "pay_real", signature);
        when(userSubscriptionRepository.findByUserIdAndRazorpayOrderId(9, "order_real")).thenReturn(Optional.of(subscription));
        when(userSubscriptionRepository.save(subscription)).thenReturn(subscription);

        SubscriptionStatusDto response = paymentService.verifyPayment(9, request);

        assertTrue(subscription.getActive());
        assertEquals("pay_real", subscription.getRazorpayPaymentId());
        assertEquals(signature, subscription.getRazorpaySignature());
        assertNotNull(subscription.getActivatedAt());
        assertEquals(9, response.getUserId());
        assertTrue(response.getActive());
        verify(userSubscriptionRepository).save(subscription);
    }

    @Test
    void verifyPaymentShouldWrapSignatureGenerationProblems() {
        UserSubscription subscription = subscription(10, false, "order_wrap");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", null);
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto("order_wrap", "pay_wrap", "anything");
        when(userSubscriptionRepository.findByUserIdAndRazorpayOrderId(10, "order_wrap")).thenReturn(Optional.of(subscription));

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.verifyPayment(10, request));

        assertEquals("Unable to verify Razorpay signature", exception.getMessage());
    }

    @Test
    void getStatusShouldReturnStoredSubscription() {
        UserSubscription subscription = subscription(11, true, "order_status");
        when(userSubscriptionRepository.findByUserId(11)).thenReturn(Optional.of(subscription));

        SubscriptionStatusDto response = paymentService.getStatus(11);

        assertTrue(response.getActive());
        assertEquals("FlowBoard Pro", response.getPlanName());
        assertEquals(3, response.getFreeWorkspaceLimit());
    }

    @Test
    void getStatusShouldReturnDefaultStatusWhenMissing() {
        when(userSubscriptionRepository.findByUserId(12)).thenReturn(Optional.empty());

        SubscriptionStatusDto response = paymentService.getStatus(12);

        assertEquals(12, response.getUserId());
        assertFalse(response.getActive());
        assertEquals("FlowBoard Pro", response.getPlanName());
        assertEquals(99900, response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals(3, response.getFreeWorkspaceLimit());
    }

    @Test
    void hasActiveSubscriptionShouldReflectRepositoryState() {
        when(userSubscriptionRepository.findByUserId(13)).thenReturn(Optional.of(subscription(13, true, "order_active")));
        when(userSubscriptionRepository.findByUserId(14)).thenReturn(Optional.empty());

        assertTrue(paymentService.hasActiveSubscription(13));
        assertFalse(paymentService.hasActiveSubscription(14));
    }


    @Test
    void createOrderShouldRejectBlankKeyIdConfiguration() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", " ");
        when(userSubscriptionRepository.findByUserId(15)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.createOrder(15));

        assertEquals("Razorpay keys are not configured. Add razorpay_key_id and razorpay_key_secret before taking payment.", exception.getMessage());
    }

    @Test
    void createOrderShouldRejectBlankSecretConfiguration() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", " ");
        when(userSubscriptionRepository.findByUserId(16)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.createOrder(16));

        assertEquals("Razorpay keys are not configured. Add razorpay_key_id and razorpay_key_secret before taking payment.", exception.getMessage());
    }
    private UserSubscription subscription(Integer userId, boolean active, String orderId) {
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setActive(active);
        subscription.setPlanName("FlowBoard Pro");
        subscription.setAmount(99900);
        subscription.setCurrency("INR");
        subscription.setRazorpayOrderId(orderId);
        return subscription;
    }
}



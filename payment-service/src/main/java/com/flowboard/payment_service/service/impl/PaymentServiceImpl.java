package com.flowboard.payment_service.service.impl;

import com.flowboard.payment_service.dto.CreateOrderResponseDto;
import com.flowboard.payment_service.dto.SubscriptionStatusDto;
import com.flowboard.payment_service.dto.VerifyPaymentRequestDto;
import com.flowboard.payment_service.entity.UserSubscription;
import com.flowboard.payment_service.exception.PaymentException;
import com.flowboard.payment_service.repository.UserSubscriptionRepository;
import com.flowboard.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private static final int FREE_WORKSPACE_LIMIT = 3;

    private final RestTemplate restTemplate;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency}")
    private String currency;

    @Value("${razorpay.workspace-upgrade-amount}")
    private Integer workspaceUpgradeAmount;

    @Value("${razorpay.workspace-upgrade-name}")
    private String workspaceUpgradeName;

    @Override
    @Transactional
    public CreateOrderResponseDto createOrder(Integer userId) {
        if (hasActiveSubscription(userId)) {
            throw new PaymentException("You already have an active subscription");
        }

        if (isPlaceholderRazorpayConfig()) {
            throw new PaymentException("Razorpay keys are not configured. Add razorpay_key_id and razorpay_key_secret before taking payment.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", workspaceUpgradeAmount);
        body.put("currency", currency);
        body.put("receipt", "flowboard-" + userId + "-" + System.currentTimeMillis());
        body.put("notes", Map.of(
                "userId", String.valueOf(userId),
                "plan", workspaceUpgradeName
        ));

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(
                    "https://api.razorpay.com/v1/orders",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
        } catch (Exception ex) {
            log.error("Razorpay order creation failed for user {}", userId, ex);
            throw new PaymentException("Unable to create Razorpay order right now. Check Razorpay key id/secret and internet access from payment-service.");
        }

        if (response == null || response.get("id") == null) {
            throw new PaymentException("Razorpay order response was empty");
        }

        String orderId = response.get("id").toString();
        UserSubscription subscription = userSubscriptionRepository.findByUserId(userId)
                .orElse(UserSubscription.builder()
                        .userId(userId)
                        .build());

        subscription.setActive(false);
        subscription.setPlanName(workspaceUpgradeName);
        subscription.setAmount(workspaceUpgradeAmount);
        subscription.setCurrency(currency);
        subscription.setRazorpayOrderId(orderId);
        userSubscriptionRepository.save(subscription);

        return CreateOrderResponseDto.builder()
                .key(razorpayKeyId)
                .orderId(orderId)
                .amount(workspaceUpgradeAmount)
                .currency(currency)
                .planName(workspaceUpgradeName)
                .userId(userId)
                .description("Upgrade to FlowBoard Pro to create more than 3 workspaces")
                .build();
    }

    @Override
    @Transactional
    public SubscriptionStatusDto verifyPayment(Integer userId, VerifyPaymentRequestDto verifyPaymentRequestDto) {
        UserSubscription subscription = userSubscriptionRepository.findByUserIdAndRazorpayOrderId(userId, verifyPaymentRequestDto.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentException("Subscription order not found"));

        String payload = verifyPaymentRequestDto.getRazorpayOrderId() + "|" + verifyPaymentRequestDto.getRazorpayPaymentId();
        String expectedSignature = hmacSha256(payload, razorpayKeySecret);

        if (!expectedSignature.equals(verifyPaymentRequestDto.getRazorpaySignature())) {
            throw new PaymentException("Razorpay signature verification failed");
        }

        subscription.setActive(true);
        subscription.setRazorpayPaymentId(verifyPaymentRequestDto.getRazorpayPaymentId());
        subscription.setRazorpaySignature(verifyPaymentRequestDto.getRazorpaySignature());
        subscription.setActivatedAt(LocalDateTime.now());
        userSubscriptionRepository.save(subscription);

        return buildStatus(subscription);
    }

    @Override
    public SubscriptionStatusDto getStatus(Integer userId) {
        return userSubscriptionRepository.findByUserId(userId)
                .map(this::buildStatus)
                .orElseGet(() -> SubscriptionStatusDto.builder()
                        .userId(userId)
                        .active(false)
                        .planName(workspaceUpgradeName)
                        .amount(workspaceUpgradeAmount)
                        .currency(currency)
                        .freeWorkspaceLimit(FREE_WORKSPACE_LIMIT)
                        .build());
    }

    @Override
    public Boolean hasActiveSubscription(Integer userId) {
        return userSubscriptionRepository.findByUserId(userId)
                .map(UserSubscription::getActive)
                .orElse(false);
    }

    private SubscriptionStatusDto buildStatus(UserSubscription subscription) {
        return SubscriptionStatusDto.builder()
                .userId(subscription.getUserId())
                .active(subscription.getActive())
                .planName(subscription.getPlanName())
                .amount(subscription.getAmount())
                .currency(subscription.getCurrency())
                .freeWorkspaceLimit(FREE_WORKSPACE_LIMIT)
                .build();
    }

    private String buildBasicAuthHeader() {
        String credentials = razorpayKeyId + ":" + razorpayKeySecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isPlaceholderRazorpayConfig() {
        return razorpayKeyId == null
                || razorpayKeyId.isBlank()
                || razorpayKeySecret == null
                || razorpayKeySecret.isBlank()
                || "rzp_test_flowboard".equals(razorpayKeyId)
                || "flowboard_test_secret".equals(razorpayKeySecret);
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new PaymentException("Unable to verify Razorpay signature");
        }
    }
}

package com.flowboard.subscription_service.service.impl;

import com.flowboard.subscription_service.dto.PaymentVerificationDto;
import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.entity.SubscriptionPlan;
import com.flowboard.subscription_service.entity.UserSubscription;
import com.flowboard.subscription_service.exception.UserNotFoundException;
import com.flowboard.subscription_service.mapper.Mapper;
import com.flowboard.subscription_service.repository.SubscriptionRepository;
import com.flowboard.subscription_service.service.RazorPayService;
import com.flowboard.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final Mapper<UserSubscription, SubscriptionResponseDto> responseMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final RazorPayService razorPayService;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    /**
     * Step 1: Create a Razorpay order only.
     * The subscription is NOT saved to the DB here.
     * The frontend must open the Razorpay checkout with the returned orderId,
     * then call /verify after a successful payment.
     */
    @Override
    public RazorPayResponseDto buySubscription(SubscriptionRequestDto subscriptionRequestDto, Integer userId) {
        log.info("Subscription purchase requested by user {}", userId);
        return razorPayService.getSubscription(subscriptionRequestDto);
    }

    /**
     * Step 2: Verify Razorpay HMAC signature, then save the subscription.
     * Razorpay signs: HMAC-SHA256(orderId + "|" + paymentId, keySecret)
     */
    @Override
    public SubscriptionResponseDto verifyAndActivate(PaymentVerificationDto dto, Integer userId) {
        log.info("Subscription verification requested by user {}", userId);
        String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();

        if (!isSignatureValid(payload, dto.getRazorpaySignature())) {
            log.error("Razorpay signature mismatch for order: {}", dto.getRazorpayOrderId());
            throw new RuntimeException("Payment verification failed: invalid signature");
        }

        SubscriptionPlan plan = dto.getPlan();
        LocalDate now = LocalDate.now();

        UserSubscription userSubscription = UserSubscription.builder()
                .userId(userId)
                .plan(plan)
                .startDate(now)
                .expiryDate(now.plusDays(plan.getDurationDays()))
                .status("Active")
                .build();

        UserSubscription saved = subscriptionRepository.save(userSubscription);
        log.info("Subscription activated for user {}", userId);

        return responseMapper.mapTo(saved);
    }

    @Override
    public SubscriptionResponseDto getDetails(Integer userId) {
        log.info("Subscription details requested for user {}", userId);
        UserSubscription userSubscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return responseMapper.mapTo(userSubscription);
    }

    @Override
    public List<SubscriptionPlanResponseDto> getPlanDetails() {
        // Prices stored in paise; divide by 100 to show rupees to the frontend
        return List.of(
                toDto(SubscriptionPlan.PRO),
                toDto(SubscriptionPlan.PREMIUM),
                toDto(SubscriptionPlan.BASIC)
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SubscriptionPlanResponseDto toDto(SubscriptionPlan plan) {
        return SubscriptionPlanResponseDto.builder()
                .durationDays(plan.getDurationDays())
                .price(plan.getPrice() / 100)
                .build();
    }

    private boolean isSignatureValid(String payload, String expectedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = HexFormat.of().formatHex(hash);
            return generated.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}

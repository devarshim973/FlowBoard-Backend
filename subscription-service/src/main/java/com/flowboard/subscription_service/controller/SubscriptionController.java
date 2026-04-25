package com.flowboard.subscription_service.controller;

import com.flowboard.subscription_service.dto.PaymentVerificationDto;
import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }
        return Integer.parseInt(userIdHeader);
    }

    /**
     * Step 1: Create a Razorpay order.
     * Returns orderId, amount, currency, keyId — frontend opens the checkout modal with these.
     */
    @PostMapping("/buy")
    public ResponseEntity<RazorPayResponseDto> buySubscription(
            @Valid @RequestBody SubscriptionRequestDto subscriptionRequestDto,
            HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.buySubscription(subscriptionRequestDto, userId));
    }

    /**
     * Step 2: Verify Razorpay payment signature and activate the subscription.
     * Called by the frontend inside Razorpay's success handler.
     */
    @PostMapping("/verify")
    public ResponseEntity<SubscriptionResponseDto> verifyPayment(
            @Valid @RequestBody PaymentVerificationDto verificationDto,
            HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.verifyAndActivate(verificationDto, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<SubscriptionResponseDto> getMySubscription(HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(subscriptionService.getDetails(userId));
    }

    @GetMapping("/details")
    public ResponseEntity<List<SubscriptionPlanResponseDto>> getPlanDetails() {
        return ResponseEntity.ok(subscriptionService.getPlanDetails());
    }
}

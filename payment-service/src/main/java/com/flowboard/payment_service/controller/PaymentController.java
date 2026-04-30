package com.flowboard.payment_service.controller;

import com.flowboard.payment_service.dto.CreateOrderResponseDto;
import com.flowboard.payment_service.dto.SubscriptionStatusDto;
import com.flowboard.payment_service.dto.VerifyPaymentRequestDto;
import com.flowboard.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Controller", description = "Payment and subscription related APIs")
public class PaymentController {
    private final PaymentService paymentService;

    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @Operation(summary = "Create Razorpay order", description = "Creates an upgrade order for the logged user")
    @ApiResponse(responseCode = "200", description = "Order created successfully")
    @PostMapping("/order")
    public ResponseEntity<CreateOrderResponseDto> createOrder(HttpServletRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(getUserId(request)));
    }

    @Operation(summary = "Verify payment", description = "Verifies Razorpay payment signature and activates subscription")
    @ApiResponse(responseCode = "200", description = "Payment verified successfully")
    @PostMapping("/verify")
    public ResponseEntity<SubscriptionStatusDto> verifyPayment(HttpServletRequest request,
                                                               @Valid @RequestBody VerifyPaymentRequestDto verifyPaymentRequestDto) {
        return ResponseEntity.ok(paymentService.verifyPayment(getUserId(request), verifyPaymentRequestDto));
    }

    @Operation(summary = "Get my subscription", description = "Returns payment status for logged user")
    @ApiResponse(responseCode = "200", description = "Status fetched successfully")
    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusDto> getStatus(HttpServletRequest request) {
        return ResponseEntity.ok(paymentService.getStatus(getUserId(request)));
    }

    @Operation(summary = "Check active subscription", description = "Internal endpoint used by workspace service")
    @ApiResponse(responseCode = "200", description = "Subscription status fetched successfully")
    @GetMapping("/check/{userId}")
    public Boolean checkSubscription(@PathVariable Integer userId) {
        return paymentService.hasActiveSubscription(userId);
    }
}

package com.flowboard.subscription_service.service.impl;

import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.service.RazorPayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RazorPayServiceImpl implements RazorPayService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Override
    public RazorPayResponseDto getSubscription(SubscriptionRequestDto subscriptionRequestDto) {
        try {
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

            log.info("Creating Razorpay order for plan: {}", subscriptionRequestDto.getPlan());

            JSONObject options = new JSONObject();
            options.put("amount", subscriptionRequestDto.getPlan().getPrice());
            options.put("currency", "INR");
            options.put("receipt", "sub_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(options);
            log.info("Razorpay order created: {}", (Object) order.get("id"));

            return RazorPayResponseDto.builder()
                    .status("SUCCESS")
                    .message("Order created successfully")
                    .orderId(order.get("id"))
                    .amount(String.valueOf(subscriptionRequestDto.getPlan().getPrice()))
                    .currency(order.get("currency"))
                    .keyId(keyId)
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for plan {}", subscriptionRequestDto.getPlan(), e);
            return RazorPayResponseDto.builder()
                    .status("FAILED")
                    .message("Unable to create payment order")
                    .build();
        }
    }
}

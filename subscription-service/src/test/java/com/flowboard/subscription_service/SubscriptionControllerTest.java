package com.flowboard.subscription_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.subscription_service.controller.SubscriptionController;
import com.flowboard.subscription_service.dto.PaymentVerificationDto;
import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.entity.SubscriptionPlan;
import com.flowboard.subscription_service.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void buySubscription_positive() throws Exception {

        SubscriptionRequestDto request = new SubscriptionRequestDto();
        request.setPlan(SubscriptionPlan.BASIC);

        RazorPayResponseDto response = new RazorPayResponseDto();
        response.setOrderId("order_123");

        when(subscriptionService.buySubscription(
                any(SubscriptionRequestDto.class),
                eq(1)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions/buy")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order_123"));
    }

    @Test
    void buySubscription_negative() throws Exception {

        mockMvc.perform(post("/api/v1/subscriptions/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyPayment_positive() throws Exception {

        PaymentVerificationDto request = new PaymentVerificationDto();
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("sig_123");
        request.setPlan(SubscriptionPlan.BASIC);

        SubscriptionResponseDto response = new SubscriptionResponseDto();
        response.setId(1);

        when(subscriptionService.verifyAndActivate(
                any(PaymentVerificationDto.class),
                eq(1)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions/verify")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void verifyPayment_negative() throws Exception {

        mockMvc.perform(post("/api/v1/subscriptions/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMySubscription_positive() throws Exception {

        SubscriptionResponseDto response = new SubscriptionResponseDto();
        response.setId(1);

        when(subscriptionService.getDetails(1))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/subscriptions/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getMySubscription_negative() throws Exception {

        mockMvc.perform(get("/api/v1/subscriptions/my"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlanDetails_positive() throws Exception {

        SubscriptionPlanResponseDto dto =
                SubscriptionPlanResponseDto.builder()
                        .durationDays(30)
                        .price(499)
                        .build();

        when(subscriptionService.getPlanDetails())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/subscriptions/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].durationDays").value(30))
                .andExpect(jsonPath("$[0].price").value(499));
    }

    @Test
    void getPlanDetails_negative() throws Exception {

        when(subscriptionService.getPlanDetails())
                .thenThrow(new RuntimeException("Failed"));

        mockMvc.perform(get("/api/v1/subscriptions/details"))
                .andExpect(status().isBadRequest());
    }
}
package com.flowboard.subscription_service;

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
import com.flowboard.subscription_service.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private Mapper<UserSubscription, SubscriptionResponseDto> responseMapper;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private RazorPayService razorPayService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void buySubscription_positive() {

        SubscriptionRequestDto request =
                new SubscriptionRequestDto();

        RazorPayResponseDto response =
                new RazorPayResponseDto();
        response.setOrderId("order_123");

        when(razorPayService.getSubscription(request))
                .thenReturn(response);

        RazorPayResponseDto result =
                subscriptionService.buySubscription(request, 1);

        assertEquals("order_123", result.getOrderId());
    }

    @Test
    void verifyAndActivate_positive() {

        ReflectionTestUtils.setField(
                subscriptionService,
                "keySecret",
                "test_secret"
        );

        PaymentVerificationDto dto =
                new PaymentVerificationDto();

        dto.setRazorpayOrderId("order1");
        dto.setRazorpayPaymentId("pay1");
        dto.setRazorpaySignature("wrong_signature");
        dto.setPlan(SubscriptionPlan.BASIC);

        assertThrows(RuntimeException.class,
                () -> subscriptionService.verifyAndActivate(dto, 1));
    }

    @Test
    void getDetails_positive() {

        UserSubscription subscription =
                UserSubscription.builder()
                        .id(1)
                        .userId(1)
                        .plan(SubscriptionPlan.BASIC)
                        .startDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusDays(30))
                        .status("Active")
                        .build();

        SubscriptionResponseDto response =
                new SubscriptionResponseDto();
        response.setId(1);

        when(subscriptionRepository.findByUserId(1))
                .thenReturn(Optional.of(subscription));

        when(responseMapper.mapTo(subscription))
                .thenReturn(response);

        SubscriptionResponseDto result =
                subscriptionService.getDetails(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getDetails_negative() {

        when(subscriptionRepository.findByUserId(99))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> subscriptionService.getDetails(99));
    }

    @Test
    void getPlanDetails_positive() {

        List<SubscriptionPlanResponseDto> result =
                subscriptionService.getPlanDetails();

        assertEquals(3, result.size());
    }
}
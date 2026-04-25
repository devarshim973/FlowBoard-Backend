package com.flowboard.subscription_service.entity;

import lombok.Getter;

/*
In stripe everything works in paisa. paisa -> INR divide by 100
 */
@Getter
public enum SubscriptionPlan {
    BASIC(30, 19900),
    PRO(90, 49900),
    PREMIUM(365, 99900);

    private final int durationDays;
    private final long price;

    SubscriptionPlan(int durationDays, long price) {
        this.durationDays = durationDays;
        this.price = price;
    }
}
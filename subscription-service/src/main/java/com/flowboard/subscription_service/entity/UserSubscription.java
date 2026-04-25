package com.flowboard.subscription_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
@Entity
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;

    private LocalDate startDate;

    private LocalDate expiryDate;

    private String status;
}
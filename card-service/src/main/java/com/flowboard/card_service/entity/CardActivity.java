package com.flowboard.card_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer activityId;

    private Integer cardId;

    private Integer actorId;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private String details;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
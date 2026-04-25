package com.flowboard.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Column(nullable = false)
    private Integer recipientId;

    @Column(nullable = false)
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String title;

    private String message;

    // Related entity id (cardId / boardId etc.)
    @Column(nullable = false)
    private Integer relatedId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedType relatedType;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime createdAt;
}

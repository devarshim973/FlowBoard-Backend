package com.flowboard.card_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/*
Index creation is done on 3 things on which we will heavily query and will rarely change
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        indexes = {
                @Index(name="idx_list_id", columnList = "listId"),
                @Index(name = "idx_boardId", columnList = "boardId"),
                @Index(name = "idx_assignee_id", columnList = "assigneeId")
        }
)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardId;

    @Column(nullable = false)
    private Integer listId;

    @Column(nullable = false)
    private Integer boardId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TO_DO;

    private LocalDateTime dueDate;

    private LocalDateTime startDate;

    private Integer assigneeId;

    @Column(nullable = false)
    private Integer createdById;

    @Column(nullable = false)
    private Boolean isArchived = false;

    @Column(nullable = false)
    private String coverColor;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
package com.flowboard.list_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        indexes = {
                @Index(name = "idx_board_id", columnList = "boardId"),
                @Index(name = "idx_board_position", columnList = "boardId, position")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class TaskList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer listId;

    @Column(nullable = false)
    private Integer boardId;

    @Column(nullable = false)
    private String name;

    /**
     Used for ordering lists in a board (drag & drop)
     */
    @Column(nullable = false)
    private Integer position;

    /*
     UI color (hex code) for each color
     */
    private String color;

    /*
      Soft delete flag, by default false
     */
    @Column(nullable = false)
    private boolean archived = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
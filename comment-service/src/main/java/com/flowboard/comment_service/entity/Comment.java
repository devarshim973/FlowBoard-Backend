package com.flowboard.comment_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @Column(nullable = false)
    private Integer cardId;

    @Column(nullable = false)
    private Integer authorId;

    @Column(nullable = false)
    private String content;

    /* currently not using this - TODO */
    @Column(nullable = false)
    private Boolean isDeleted = false;

    /*
    can be null for top level comment and also only supports 2 level of nesting
     */
    private Integer parentCommentId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
